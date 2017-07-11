package soulyaroslav.library.entity

import soulyaroslav.library.util.FieldType
import soulyaroslav.library.validation.IValidator

/**
 * Created by yaroslav on 7/6/17.
 */
class FieldBuilder {
    private var fields: ArrayList<Field> = ArrayList()

    fun addField(fieldType: FieldType, validator: IValidator) : FieldBuilder {
        fields.add(Field(fieldType, validator))
        return this
    }

    fun build() : ArrayList<Field> {
        return fields
    }
}