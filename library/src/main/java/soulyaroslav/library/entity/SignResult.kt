package soulyaroslav.library.entity

/**
 * Created by yaroslav on 7/6/17.
 */
data class SignResult(val name: String, val email: String, val password: String) {

    override fun toString(): String {
        return "Name - " + name + "\n" +
                "Email - " + email + "\n" +
                "Password - " + password + "\n"

    }
}