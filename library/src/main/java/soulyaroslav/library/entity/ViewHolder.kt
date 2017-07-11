package soulyaroslav.library.entity

import android.view.View
import android.widget.EditText
import android.widget.TextView
import soulyaroslav.library.R

/**
 * Created by yaroslav on 7/4/17.
 */
class ViewHolder(val root: View) {

    val textViewField : TextView = root.findViewById(R.id.textViewField) as TextView
    val editTextFiled : EditText = root.findViewById(R.id.editTextField) as EditText

}