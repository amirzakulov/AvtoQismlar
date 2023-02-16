package uz.almirab.avtoqismlar.models

data class LoggedInData (
    val logged_in: Boolean,
    val fullName: String,
    var phone: String,
    var address: String,
    val access_token: String,
    val token_type: String
)

data class PhoneVerificationData (
    val isPhoneRegistered: Boolean,
    val errorMessage: String
)


data class UserRegistrationData (
    val phoneExist: Boolean,
    val errors: Boolean,
    val errorMessages: List<String>,
    val user: UserData
)

data class UserData (
    var fullName: String,
    var phone: String,
    var address: String,
    var fcmToken: String,
    var deviceName: String,
)

data class VerificationCodeData (
    val code: String,
    val phone: String,
    val deviceName: String
)

data class VerifiedUserData (
    val codeVerified: Boolean,
    val client_id: Int,
    val fullName: String,
    val phone: String,
    val address: String,
    val token: String,
    val token_type: String,
)

data class UserEditData (
    val client_id: Int,
    val userField: String,
    val userDetail: String
)

