package soulyaroslav.library.validation

/**
 * Created by yaroslav on 7/6/17.
 */
class DefaultNameValidator : IValidator {

    override fun validate(target: String): Boolean {
        return !target.isNullOrEmpty()
    }
}