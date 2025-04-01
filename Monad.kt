package com.damianlattenero.typeclasses

import arrow.Kind

interface Monad<F> {
    fun <A> just(a: A): Kind<F, A>
    fun <A, B> Kind<F, A>.flatMap(f: (A) -> Kind<F, B>): Kind<F, B>
    fun <A, B> Kind<F, A>.map(f: (A) -> B): Kind<F, B>
}