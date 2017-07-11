package soulyaroslav.library.validation

import android.util.Patterns

/**
 * Created by yaroslav on 7/6/17.
 */
class DefaultEmailValidator : IValidator {
    override fun validate(target: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }
}
