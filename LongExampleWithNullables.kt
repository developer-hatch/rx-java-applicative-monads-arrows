package com.damianlattenero.examples

import arrow.Kind
import com.damianlattenero.dsl.*
import com.damianlattenero.instances.MonadSingle
import com.damianlattenero.typeclasses.Monad
import io.reactivex.rxjava3.core.Single

data class MegaUser(
    val name: String?,
    val email: String?,
    val phone: String?,
    val age: Int?,
    val address: String?,
    val role: String?,
    val metadata: String?,
    val status: String?,
    val note: String?,
    val tag: String?
)

fun curriedMegaUser(): (String?) -> (String?) -> (String?) -> (Int?) -> (String?) -> (String?) -> (String?) -> (String?) -> (String?) -> (String?) -> MegaUser =
    { name -> { email -> { phone -> { age -> { address -> { role -> { metadata -> { status -> { note -> { tag ->
        MegaUser(name, email, phone, age, address, role, metadata, status, note, tag)
    }}}}}}}}

fun fetchNullableName(): Single<String?> = Single.just("Alice")
fun fetchNullableEmail(): Single<String?> = Single.just(null)
fun fetchNullablePhone(): Single<String?> = Single.just("123-456")
fun fetchNullableAge(): Single<Int?> = Single.just(30)
fun fetchNullableAddress(): Single<String?> = Single.just("Wonderland")
fun fetchNullableRole(): Single<String?>? = null
fun fetchNullableMetadata(): Single<String?>? = Single.just("meta")
fun fetchNullableStatus(): Single<String?>? = Single.just("active")
fun fetchNullableNote(): Single<String?>? = null
fun fetchNullableTag(): Single<String?>? = Single.just("tag")

fun main() {
    with(MonadSingle) {
        val result =
            (::curriedMegaUser).just()
                .zipApWith(fetchNullableName())
                .zipApWith(fetchNullableEmail())
                .zipApWith(fetchNullablePhone())
                .flatApWith(fetchNullableAge())
                .flatApWith(fetchNullableAddress())
                .zipApWithNullable(fetchNullableRole())
                .flatApWithNullable(fetchNullableMetadata())
                .zipApWithNullable(fetchNullableStatus())
                .flatApWithNullable(fetchNullableNote())
                .zipApWithNullable(fetchNullableTag())

        (result as Single<MegaUser>).subscribe { println("User: $it") }
    }
}