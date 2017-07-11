package soulyaroslav.library.entity

import soulyaroslav.library.util.FieldType
import soulyaroslav.library.validation.IValidator

/**
 * Created by yaroslav on 7/6/17.
 */
class Field(val fieldType: FieldType, val validator: IValidator)