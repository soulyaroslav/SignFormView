package soulyaroslav.library.validation

/**
 * Created by yaroslav on 7/6/17.
 */
open interface IValidator {
    fun validate(target: String) : Boolean
}