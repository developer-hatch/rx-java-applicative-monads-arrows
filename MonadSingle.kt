package com.damianlattenero.instances

import arrow.Kind
import com.damianlattenero.typeclasses.Monad
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.functions.BiFunction

object MonadSingle : Monad<ForSingle> {
    override fun <A> just(a: A): Single<A> = Single.just(a)

    override fun <A, B> Kind<ForSingle, A>.flatMap(f: (A) -> Kind<ForSingle, B>): Single<B> =
        (this as Single<A>).flatMap { f(it) as Single<B> }

    override fun <A, B> Kind<ForSingle, A>.map(f: (A) -> B): Single<B> =
        (this as Single<A>).map(f)
}

typealias ForSingle = Single<*>