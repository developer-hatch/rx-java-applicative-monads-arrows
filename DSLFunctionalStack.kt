
package com.example.dsl

import arrow.Kind
import arrow.core.*
import arrow.mtl.*
import arrow.typeclasses.*
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.flow.Flow

// === Common Types ===

data class FieldError(val field: String, val message: String)

// === Monad + Lift for SingleK ===

class ForSingleK private constructor()
typealias SingleKOf<A> = Kind<ForSingleK, A>
data class SingleK<A>(val value: Single<A>) : SingleKOf<A>
fun <A> SingleKOf<A>.fix(): SingleK<A> = this as SingleK<A>

object MonadSingleK : Monad<ForSingleK> {
    override fun <A> just(a: A): SingleKOf<A> = SingleK(Single.just(a))
    override fun <A, B> Kind<ForSingleK, A>.flatMap(f: (A) -> Kind<ForSingleK, B>): SingleKOf<B> =
        fix().value.flatMap { (f(it) as SingleK<B>).value }.let { SingleK(it) }
    override fun <A, B> Kind<ForSingleK, A>.map(f: (A) -> B): SingleKOf<B> =
        fix().value.map(f).let { SingleK(it) }
}

// === Monad + Lift for ObservableK (placeholder, user to complete) ===

class ForObservableK private constructor()
typealias ObservableKOf<A> = Kind<ForObservableK, A>
// Add ObservableK wrapper + MonadObservableK if needed

// === Monad + Lift for FlowK (placeholder, user to complete) ===

class ForFlowK private constructor()
typealias FlowKOf<A> = Kind<ForFlowK, A>
// Add FlowK wrapper + MonadFlowK if needed

// === Monad Transformer Lifts ===

object LiftMaybeTSingle : Lift<MaybeTPartialOf<ForSingleK>, Single<*>> {
    @Suppress("UNCHECKED_CAST")
    override fun <A> Single<*>.lift(): MaybeTOf<ForSingleK, A> =
        MaybeT(MaybeK(this as Single<A>).k())
}

object LiftMaybeTSingleValidated : Lift<MaybeTPartialOf<ForSingleK>, Single<*>> {
    @Suppress("UNCHECKED_CAST")
    override fun <A> Single<*>.lift(): MaybeTOf<ForSingleK, ValidatedNel<FieldError, A>> =
        MaybeT(MaybeK(this as Single<ValidatedNel<FieldError, A>>).k())
}

// === Functional Blocks ===

inline fun <A> withMaybeSingle(
    block: Monad<MaybeTPartialOf<ForSingleK>>.(Lift<MaybeTPartialOf<ForSingleK>, Single<*>>) -> MaybeTOf<ForSingleK, A>
): MaybeTOf<ForSingleK, A> =
    MonadMaybeT(MonadSingleK).block(LiftMaybeTSingle)

inline fun <A> withValidatedMaybeSingle(
    block: Monad<MaybeTPartialOf<ForSingleK>>.(Lift<MaybeTPartialOf<ForSingleK>, Single<*>>) -> MaybeTOf<ForSingleK, ValidatedNel<FieldError, A>>
): MaybeTOf<ForSingleK, ValidatedNel<FieldError, A>> =
    MonadMaybeT(MonadSingleK).block(LiftMaybeTSingleValidated)

// === Add other combinations as needed ===
