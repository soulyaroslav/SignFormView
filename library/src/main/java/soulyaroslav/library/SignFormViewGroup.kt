package soulyaroslav.library

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import soulyaroslav.library.entity.Field
import soulyaroslav.library.entity.FieldHolder
import soulyaroslav.library.entity.SignResult
import soulyaroslav.library.entity.ViewHolder
import soulyaroslav.library.extension.*
import soulyaroslav.library.listener.SignFormListener
import soulyaroslav.library.util.BounceInterpolator
import soulyaroslav.library.util.Constants
import soulyaroslav.library.util.FieldType
import soulyaroslav.library.view.FieldIconView
import soulyaroslav.library.view.NextIconView
import java.util.*
import android.widget.TextView


/**
 * Created by yaroslav on 7/4/17.
 */

class SignFormViewGroup : ViewGroup {

    private val AMPLITUDE = .5
    private val FREQUENCY = 5
    // input method instance
    private lateinit var inputMethodManager : InputMethodManager
    // handler for update view state after some time
    private val updateHandler: Handler = Handler()
    // parent background paint
    private lateinit var backgroundPaint : Paint
    // next view background paint
    private lateinit var nextBackgroundPaint : Paint
    private lateinit var maskPaint : Paint
    // paint for all text
    private lateinit var textPaint : TextPaint
    // parent width and height
    private var viewWidth = defaultInit()
    private var viewHeight = defaultInit()
    // list of field holders
    private var fieldHolders: ArrayList<FieldHolder> = ArrayList()
    // title views
    private lateinit var mainTitleView: View
    private lateinit var finishTitleView: View
    // view field holders
    private lateinit var nameFieldHolder: FieldHolder
    private lateinit var emailFieldHolder: FieldHolder
    private lateinit var passwordFieldHolder: FieldHolder
    // sign form listener
    private var listener : SignFormListener? = null
    // validate sign result
    private var name: String = ""
    private var email: String = ""
    private var password: String = ""
    // current field type
    private var currentFieldType = FieldType.NAME_FIELD
    // parent padding
    private var viewPadding = defaultInit()
    // text size in the sign fields after change
    private var fieldTextWidth = 1
    private var previousFullTextSize = 0
    private var previousFieldTextWidth = 0
    // icon when all data entered
    private lateinit var nextIconView: NextIconView
    // flag for drawing main title
    private var isSignFinish = false

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        retrieveDefaultSize()
        initTextPaint()
        initParentBackgroundPaint()
        initNextBackgroundPaint()


    }

    private fun initParentBackgroundPaint() {
        backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        backgroundPaint.apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = resources.getDimensionPixelSize(R.dimen.stroke_width).toFloat()
        }
    }

    private fun initNextBackgroundPaint() {
        nextBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        nextBackgroundPaint.apply {
            color = ContextCompat.getColor(context, R.color.next_background)
            style = Paint.Style.FILL
        }

        maskPaint = Paint()
        maskPaint.apply {
            color = ContextCompat.getColor(context, R.color.next_background)
            style = Paint.Style.FILL
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        }
    }

    private fun initTextPaint() {
        textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        textPaint.apply {
            textSize = resources.getDimensionPixelSize(R.dimen.text_size).toFloat()
            color = Color.WHITE
        }
    }

    private fun retrieveDefaultSize() {
        viewWidth = resources.getDimensionPixelSize(R.dimen.view_width)
        viewHeight = resources.getDimensionPixelSize(R.dimen.view_height)
        viewPadding  = resources.getDimensionPixelSize(R.dimen.parent_padding)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        // prepare main title
        prepareTitleViews()
        // prepare next icon view
        prepareNextIconView()
    }

    private fun prepareTitleViews() {
        mainTitleView = inflate(R.layout.main_title_layout)
        finishTitleView = inflate(R.layout.finish_sign_layout)
        finishTitleView.visibility = View.GONE
        finishTitleView.scaleX = .5f
        finishTitleView.scaleY = .5f
        mainTitleView.onClickEvent { onMainTitleClick() }
        addView(mainTitleView)
        addView(finishTitleView)
    }

    private fun prepareFieldHolders(fields: ArrayList<Field>) {
        fields.forEach {
            when(it.fieldType) {
                FieldType.NAME_FIELD -> {
                    // inflate name field view
                    val nameViewHolder = initFieldView(R.layout.name_field_layout)
                    val nameIconView = initIconView(R.color.icon1, R.drawable.person)
                    nameFieldHolder = FieldHolder(nameIconView, nameViewHolder, it.validator)
                    fieldHolders.add(nameFieldHolder)
                }
                FieldType.EMAIL_FIELD -> {
                    // inflate email field view
                    val emailViewHolder = initFieldView(R.layout.email_field_layout)
                    val emailIconView = initIconView(R.color.icon2, R.drawable.mail)
                    emailFieldHolder = FieldHolder(emailIconView, emailViewHolder, it.validator)
                    fieldHolders.add(emailFieldHolder)
                }
                FieldType.PASSWORD_FIELD -> {
                    // inflate password field view
                    val passwordViewHolder = initFieldView(R.layout.password_field_layout)
                    val passwordIconView = initIconView(R.color.icon3, R.drawable.lock)
                    passwordFieldHolder = FieldHolder(passwordIconView, passwordViewHolder, it.validator)
                    fieldHolders.add(passwordFieldHolder)
                }

                else -> { /*no need*/ }
            }
        }
    }

    private fun prepareNextIconView() {
        nextIconView = NextIconView(context)
        nextIconView.apply {
            setPadding(viewPadding)
            visibility = View.GONE
            onClickEvent {
                onNextIconClickEvent()
            }
        }
        addView(nextIconView)
    }

    private fun initFieldView(layoutId: Int) : ViewHolder {
        val view = inflate(layoutId)
        val viewHolder = ViewHolder(view)
        viewHolder.apply {
            root.visibility = View.GONE
        }
        addView(view)
        return viewHolder
    }

    private fun initIconView(strokeColor: Int, icon: Int) : FieldIconView {
        val view = FieldIconView(context)
        view.apply {
            setPadding(viewPadding)
            setStrokeColor(strokeColor)
            setFieldIcon(icon)
        }
        addView(view)
        return view
    }

    private fun addTextWatcher(editText: EditText) {
        editText.onTextChange({ // first method scope
                    nextIconView.rotate(Constants.Degree.DEGREE_0,
                            Constants.Degree.DEGREE_30, Constants.DURATION_400, null)
                    onTextChange()

        })
//        nextIconView.rotate(Constants.Degree.DEGREE_30,
//                Constants.Degree.DEGREE_0, Constants.DURATION_400, BounceInterpolator(AMPLITUDE, FREQUENCY))
    }


    private fun onTextChange() {
        val viewWidth = obtainFieldWidth()
        if(viewWidth > width * .3f) {
            fieldTextWidth += viewWidth - previousFieldTextWidth
        }
        previousFieldTextWidth = viewWidth
    }

    private fun obtainFieldWidth(): Int {
        val fieldHolder: FieldHolder
        when(currentFieldType) {
            FieldType.NONE -> return 0
            FieldType.NAME_FIELD ->
                fieldHolder = nameFieldHolder
            FieldType.EMAIL_FIELD ->
                fieldHolder = emailFieldHolder
            FieldType.PASSWORD_FIELD ->
                fieldHolder = passwordFieldHolder
        }
        return fieldHolder.viewHolder.editTextFiled.width
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val parentWidth = MeasureSpec.makeMeasureSpec(viewWidth + fieldTextWidth, MeasureSpec.EXACTLY)
        val parentHeight = MeasureSpec.makeMeasureSpec(viewHeight, MeasureSpec.EXACTLY)
        measureChild(mainTitleView, viewWidth, viewHeight)
        measureChild(finishTitleView, viewWidth, viewHeight)
        // measure root view icons
        measureFieldViews()
        // measure next icon view
        measureNextIconView()
        setMeasuredDimension(parentWidth, parentHeight)
    }

    private fun measureFieldViews() {
        fieldHolders.forEach {
            it.fieldFieldIcon.measure(height, height)
            measureChild(it.viewHolder.root, viewWidth, viewHeight)
        }
    }

    private fun measureNextIconView() {
        nextIconView.measure(height / 2, height / 2)
    }

    override fun onLayout(b: Boolean, i: Int, i1: Int, i2: Int, i3: Int) {
        layoutChildTextViews(mainTitleView as TextView)
        layoutChildTextViews(finishTitleView as TextView)
        layoutFieldViews()
        layoutNextIconView()
    }

    private fun layoutChildTextViews(view: TextView) {
        val left = width / 2 - view.measuredWidth / 2
        val top = height / 2 - view.measuredHeight / 2
        val right = width / 2 + view.measuredWidth / 2
        val bottom = height / 2 + view.measuredHeight / 2
        view.layout(left, top, right, bottom)
    }

    private fun layoutRootView(root: View) {
        val centerY = height * .5f
        val left = (height * .5f).toInt() * 2
        val top = (centerY - root.measuredHeight * .5f).toInt()
        val right = (root.measuredWidth + height * .5f).toInt() * 2
        val bottom = (centerY + root.measuredHeight * .5f).toInt()
        root.layout(left, top, right, bottom)
    }

    private fun layoutFieldViews() {
        var offset = 0
        fieldHolders.forEach {
            it.fieldFieldIcon.layout(-height - offset, 0, -offset, height)
            layoutRootView(it.viewHolder.root)
            offset += 5
        }
    }

    private fun layoutNextIconView() {
        val left = width - height / 2 - nextIconView.measuredHeight / 2
        val top = height / 2 - nextIconView.measuredHeight / 2
        val right = left + nextIconView.measuredWidth
        val bottom = height / 2 + nextIconView.measuredHeight / 2
        nextIconView.layout(left, top, right, bottom)
    }

    private fun onMainTitleClick() {
        translateFieldIcons()
        rotateFieldIcons()
        setFocusListener()
        nameFieldHolder.viewHolder.root.visibility = View.VISIBLE
        nextIconView.visibility = View.VISIBLE
        mainTitleView.visibility = View.GONE
    }

    private fun setFocusListener() {
        nameFieldHolder.viewHolder.editTextFiled.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            nameFieldHolder.viewHolder.editTextFiled.post({
                inputMethodManager.showSoftInput(nameFieldHolder.viewHolder.editTextFiled, InputMethodManager.SHOW_IMPLICIT)
            })
        }
        nameFieldHolder.viewHolder.editTextFiled.requestFocus()
    }

    private fun onNextIconClickEvent() {
        when(currentFieldType) {
            FieldType.NAME_FIELD -> {
                onNameFieldValidate()
            }
            FieldType.EMAIL_FIELD -> {
                onEmailFieldValidate()
            }
            FieldType.PASSWORD_FIELD -> {
                onPasswordFieldValidate()
            }
            else -> {
                // no need
            }
        }
        fieldTextWidth = 1
    }

    private fun onSignFinish() {
        if(currentFieldType == FieldType.NONE) {
            nextIconView.visibility = View.GONE
            // update client code about sign in/up finish
            val signResult = SignResult(name, email, password)
            listener?.onSign(signResult)

            isSignFinish = true
            finishTitleView.visibility = View.VISIBLE
            finishTitleView.scaleX(1f, Constants.DURATION_800, OvershootInterpolator())
            finishTitleView.scaleY(1f, Constants.DURATION_800, OvershootInterpolator())
                    .onEnd {
                        hideKeyboard()
                        // set all views to default state
                        updateToDefaultState()
                    }

        }
    }

    private fun updateToDefaultState() {
        updateHandler.postDelayed({
            isSignFinish = false
            allViewToDefaultState()
        }, Constants.DURATION_1000)
    }

    private fun hideKeyboard() {
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }

    private fun onNameFieldValidate() {
        val nameValidator = nameFieldHolder.validator
        name = nameFieldHolder.viewHolder.editTextFiled.text.toString()
        if(nameValidator.validate(name)) {
            nameFieldHolder.fieldFieldIcon.translateX(width * 1.5f, Constants.DURATION_500, 0, AccelerateInterpolator())
                    .onEnd {
                        nameFieldHolder.fieldFieldIcon.visibility = View.GONE
                        nameFieldHolder.fieldFieldIcon.setStrokeColor(R.color.icon1)
                    }
            nameFieldHolder.fieldFieldIcon.rotate(Constants.Degree.DEGREE_0,
                    Constants.Degree.DEGREE_30, Constants.DURATION_500, DecelerateInterpolator())
            nameFieldHolder.viewHolder.root.visibility = View.GONE
            emailFieldHolder.viewHolder.root.visibility = View.VISIBLE
            currentFieldType = FieldType.EMAIL_FIELD
            // activate field icon
            activateFieldIcon(emailFieldHolder, passwordFieldHolder)
        } else {
            shakeParent()
        }
    }

    private fun onEmailFieldValidate() {
        val emailValidator = emailFieldHolder.validator
        email = emailFieldHolder.viewHolder.editTextFiled.text.toString()
        if(emailValidator.validate(email)) {
            emailFieldHolder.fieldFieldIcon.translateX(width * 1.5f, Constants.DURATION_500, 0, AccelerateInterpolator())
                    .onEnd {
                        emailFieldHolder.fieldFieldIcon.visibility = View.GONE
                        emailFieldHolder.fieldFieldIcon.setStrokeColor(R.color.icon2)
                    }
            emailFieldHolder.fieldFieldIcon.rotate(Constants.Degree.DEGREE_0,
                    Constants.Degree.DEGREE_30, Constants.DURATION_500, DecelerateInterpolator())
            emailFieldHolder.viewHolder.root.visibility = View.GONE
            passwordFieldHolder.viewHolder.root.visibility = View.VISIBLE
            currentFieldType = FieldType.PASSWORD_FIELD
            // activate field icon
            activateFieldIcon(passwordFieldHolder, null)
        } else {
            shakeParent()
        }
    }

    private fun onPasswordFieldValidate() {
        val passwordValidator = passwordFieldHolder.validator
        password = passwordFieldHolder.viewHolder.editTextFiled.text.toString()
        if(passwordValidator.validate(password)) {
            passwordFieldHolder.fieldFieldIcon.translateX(width * 1.5f, Constants.DURATION_500, 0, AccelerateInterpolator())
                    .onEnd {
                        passwordFieldHolder.fieldFieldIcon.visibility = View.GONE
                        passwordFieldHolder.fieldFieldIcon.setStrokeColor(R.color.icon3)
                        // sign logic finish
                        onSignFinish()
                    }
            passwordFieldHolder.fieldFieldIcon.rotate(Constants.Degree.DEGREE_0,
                    Constants.Degree.DEGREE_30, Constants.DURATION_500, DecelerateInterpolator())
            passwordFieldHolder.viewHolder.root.visibility = View.GONE
            currentFieldType = FieldType.NONE
        } else {
            shakeParent()
        }
    }

    private fun shakeParent() {
        shake(0f, 10f, 0f, -10f, 0f, Constants.REPEAT_COUNT, Constants.DURATION_150, DecelerateInterpolator())
        nextIconView.shake(0f, 5f, 0f, -5f, 0f, Constants.REPEAT_COUNT, Constants.DURATION_200, DecelerateInterpolator())
    }

    private fun allViewToDefaultState() {
        // translate to default
        nameFieldHolder.fieldFieldIcon.translateX(0f, 0, 0, DecelerateInterpolator())
        nameFieldHolder.viewHolder.editTextFiled.setText(Constants.EMPTY_STRING)
        emailFieldHolder.fieldFieldIcon.translateX(0f, 0, 0, DecelerateInterpolator())
        emailFieldHolder.viewHolder.editTextFiled.setText(Constants.EMPTY_STRING)
        passwordFieldHolder.fieldFieldIcon.translateX(0f, 0, 0, DecelerateInterpolator())
        passwordFieldHolder.viewHolder.editTextFiled.setText(Constants.EMPTY_STRING)
        // set main title visible
        mainTitleView.visibility = View.VISIBLE
        finishTitleView.visibility = View.GONE
        finishTitleView.scaleX = .5f
        finishTitleView.scaleY = .5f
        // set current field type
        currentFieldType = FieldType.NAME_FIELD
    }

    private fun activateFieldIcon(fieldHolder: FieldHolder, nextFieldHolder: FieldHolder?) {
        fieldHolder.fieldFieldIcon.x = fieldHolder.fieldFieldIcon.x - 5f
        if(nextFieldHolder != null) {
            nextFieldHolder.fieldFieldIcon.x = nextFieldHolder.fieldFieldIcon.x - 5f
        }
        fieldHolder.fieldFieldIcon.setStrokeColor(android.R.color.white)
    }

    private fun translateFieldIcons() {
        var delay = 0L
        fieldHolders.forEach {
            val x = height.toFloat() + 10
            it.fieldFieldIcon.visibility = View.VISIBLE
            it.fieldFieldIcon.translateX(x, Constants.DURATION_500, delay, DecelerateInterpolator())
            delay += 50
        }
    }

    private fun rotateFieldIcons() {
        fieldHolders.forEach {
            // rotate current view
            it.fieldFieldIcon.rotate(Constants.Degree.DEGREE_0, Constants.Degree.DEGREE_30, Constants.DURATION_500, null)
                    // it return ObjectAnimator, so we can add some listeners
                    .onEnd {
                        rotateFieldIconsBack()
                    }
        }
    }

    private fun rotateFieldIconsBack() {
        fieldHolders.forEach { it.fieldFieldIcon.rotate(Constants.Degree.DEGREE_30,
                Constants.Degree.DEGREE_0, Constants.DURATION_500, BounceInterpolator(AMPLITUDE, FREQUENCY)) }
    }

    override fun dispatchDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)
        drawMask(canvas)
        drawBackground(canvas)
    }

    private fun drawMask(canvas: Canvas?) {
        val mask = createMask()
        canvas?.drawBitmap(mask, 0f, 0f, maskPaint)
    }

    private fun drawBackground(canvas: Canvas?) {
        if(!isSignFinish) {
            val radius = height * .5f
            val left = viewPadding .toFloat()
            val right = width.toFloat() - viewPadding .toFloat()
            val bottom = height.toFloat() - viewPadding .toFloat()
            canvas?.drawRoundRect(left, left, right, bottom, radius, radius, backgroundPaint)
        }
    }

    private fun defaultInit() : Int = 0

    private fun createMask() : Bitmap {
        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.color = ContextCompat.getColor(context, R.color.background)
        setLayerType(View.LAYER_TYPE_HARDWARE, paint)
        // create bitmap
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        // prepare bitmap position
        val radius = height * .5f
        val right = width.toFloat()
        val bottom = height.toFloat()
        canvas.drawRoundRect(0f, 0f, right, bottom, radius, radius, paint)
        // return bitmap mask
        return bitmap
    }

//    private fun convertToBitmap(drawable: Drawable?): Bitmap? {
//        if (drawable != null) {
//            val width = drawable.intrinsicWidth
//            val height = drawable.intrinsicHeight
//            val mutableBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
//            val canvas = Canvas(mutableBitmap)
//            drawable.setBounds(0, 0, width, height)
//            drawable.draw(canvas)
//            return mutableBitmap
//        }
//        return null
//    }

    fun setFields(fields: ArrayList<Field>) {
        // prepare field holder and its view
        prepareFieldHolders(fields)
        // add text watcher for edit text
        addTextWatcher(passwordFieldHolder.viewHolder.editTextFiled)
        addTextWatcher(emailFieldHolder.viewHolder.editTextFiled)
        addTextWatcher(nameFieldHolder.viewHolder.editTextFiled)
//        nameFieldHolder.viewHolder.editTextFiled.requestFocus()
    }

    fun setSignFormListener(listener: SignFormListener) {
        this.listener = listener
    }
}
