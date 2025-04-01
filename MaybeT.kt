package com.damianlattenero.transformers

data class MaybeT<F, A>(val value: Kind<F, A?>)