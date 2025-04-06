
package com.example.dsl

import arrow.core.*
import arrow.core.extensions.*
import arrow.typeclasses.*
import arrow.mtl.*
import arrow.mtl.extensions.*
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit

// === Models ===

data class FieldError(val field: String, val message: String)

sealed class ValidatedResult<out A> {
    object Empty : ValidatedResult<Nothing>()
    data class Valid<A>(val value: A) : ValidatedResult<A>()
    data class Invalid(val errors: NonEmptyList<FieldError>) : ValidatedResult<Nothing>()
}

data class MixedUser(
    val step1: String,
    val step2: String,
    val step3: String
)

// === EffectType Enum ===

enum class EffectType {
    Validated,
    Maybe,
    Observable
}

// === Lift Infrastructure ===

typealias ForObservableK = arrow.fx.rx2.ForObservableK
typealias ObservableKOf<A> = arrow.Kind<ForObservableK, A>

fun <A> ObservableKOf<A>.fix(): arrow.fx.rx2.ObservableK<A> = this as arrow.fx.rx2.ObservableK<A>

object LiftMaybeTObservableValidatedFieldError : Lift<MaybeTPartialOf<ForObservableK>, Observable<*>> {
    @Suppress("UNCHECKED_CAST")
    override fun <A> Observable<*>.lift(): MaybeTOf<ForObservableK, ValidatedNel<FieldError, A>> =
        MaybeT(MaybeK(this as Observable<ValidatedNel<FieldError, A>>).k())
}

// === Monad Instance ===

object MonadObservableK : Monad<ForObservableK> {
    override fun <A> just(a: A): ObservableKOf<A> =
        arrow.fx.rx2.ObservableK.just(a)

    override fun <A, B> Kind<ForObservableK, A>.flatMap(f: (A) -> Kind<ForObservableK, B>): ObservableKOf<B> =
        fix().flatMap { f(it).fix() }

    override fun <A, B> Kind<ForObservableK, A>.map(f: (A) -> B): ObservableKOf<B> =
        fix().map(f)
}

// === DSL Core ===

object StackRegistry {
    fun <A> resolve(vararg effects: EffectType): Pair<Monad<MaybeTPartialOf<ForObservableK>>, Lift<MaybeTPartialOf<ForObservableK>, Any>> {
        return when (effects.toList()) {
            listOf(EffectType.Validated, EffectType.Maybe, EffectType.Observable) ->
                MonadMaybeT(MonadObservableK) to LiftMaybeTObservableValidatedFieldError
            else -> error("Unsupported stack: ${effects.joinToString(" -> ")}")
        }
    }
}

inline fun <A> withStack(
    vararg effects: EffectType,
    block: Monad<MaybeTPartialOf<ForObservableK>>.(Lift<MaybeTPartialOf<ForObservableK>, Any>) -> MaybeTOf<ForObservableK, ValidatedNel<FieldError, A>>
): MaybeTOf<ForObservableK, ValidatedNel<FieldError, A>> {
    val (monad, lift) = StackRegistry.resolve<A>(*effects)
    return monad.block(lift)
}

fun <A> MaybeTOf<ForObservableK, ValidatedNel<FieldError, A>>.result(): Observable<ValidatedResult<A>> =
    this.fix().value.fix().map { maybeValidated ->
        when {
            maybeValidated.isEmpty -> ValidatedResult.Empty
            maybeValidated.isPresent -> when (val validated = maybeValidated.orNull()!!) {
                is Validated.Valid -> ValidatedResult.Valid(validated.value)
                is Validated.Invalid -> ValidatedResult.Invalid(validated.value)
            }
            else -> ValidatedResult.Empty
        }
    }

// === Demo ===

fun step(id: Int, delayMs: Long, value: String): Observable<ValidatedNel<FieldError, String>> =
    Observable.just(value.validNel()).delay(delayMs.toLong(), TimeUnit.MILLISECONDS)
        .doOnSubscribe { println("[$id] Starting") }
        .doOnNext { println("[$id] Finished") }

fun main() {
    val result = withStack(
        EffectType.Validated,
        EffectType.Maybe,
        EffectType.Observable
    ) { monad, lift ->
        with(monad) {
            with(lift) {
                ::MixedUser.curried().just()
                    .zipApWith(step(1, 300, "A"))
                    .zipApWith(step(2, 200, "B"))
                    .zipApWith(step(3, 100, "C"))
            }
        }
    }

    result.result().subscribe { println("Final result: $it") }
}
