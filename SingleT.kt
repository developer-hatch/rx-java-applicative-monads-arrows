package com.damianlattenero.transformers

data class SingleT<F, A>(val value: Kind<F, Single<A>>)