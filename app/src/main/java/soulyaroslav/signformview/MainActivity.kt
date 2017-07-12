package soulyaroslav.signformview

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import soulyaroslav.library.SignFormViewGroup
import soulyaroslav.library.entity.FieldBuilder
import soulyaroslav.library.entity.SignResult
import soulyaroslav.library.listener.SignFormListener
import soulyaroslav.library.util.FieldType
import soulyaroslav.library.validation.DefaultEmailValidator
import soulyaroslav.library.validation.DefaultNameValidator
import soulyaroslav.library.validation.DefaultPasswordValidator

class MainActivity : AppCompatActivity(), SignFormListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val signInForm = findViewById(R.id.signInForm) as SignFormViewGroup

        val fields = FieldBuilder()
                .addField(FieldType.PASSWORD_FIELD, DefaultPasswordValidator())
                .addField(FieldType.EMAIL_FIELD, DefaultEmailValidator())
                .addField(FieldType.NAME_FIELD, DefaultNameValidator())
                .build()

        signInForm.setFields(fields)
        signInForm.setSignFormListener(this)
    }

    override fun onSign(result: SignResult) {
        Toast.makeText(this, result.toString(), Toast.LENGTH_SHORT).show()
    }
}
