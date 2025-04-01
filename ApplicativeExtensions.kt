package com.damianlattenero.dsl

import arrow.Kind
import arrow.typeclasses.Monad

context(Monad<F>)
infix fun <F, A, B> Kind<F, (A) -> B>.zipApWith(value: Kind<F, A>): Kind<F, B> =
    flatMap { f -> value.map { a -> f(a) } }

context(Monad<F>)
infix fun <F, A, B> Kind<F, (A?) -> B>.zipApWithNullable(value: Kind<F, A>?): Kind<F, B> =
    if (value == null) map { it(null) } else flatMap { f -> value.map { a -> f(a) } }

context(Monad<F>)
infix fun <F, A, B> Kind<F, (A) -> B>.flatApWith(value: Kind<F, A>): Kind<F, B> =
    flatMap { f -> value.map { a -> f(a) } }

context(Monad<F>)
infix fun <F, A, B> Kind<F, (A?) -> B>.flatApWithNullable(value: Kind<F, A>?): Kind<F, B> =
    if (value == null) map { it(null) } else flatMap { f -> value.map { a -> f(a) } }