package soulyaroslav.library.validation

/**
 * Created by yaroslav on 7/6/17.
 */
class DefaultPasswordValidator : IValidator {
    override fun validate(target: String): Boolean {
        return target.length > 6
    }
}