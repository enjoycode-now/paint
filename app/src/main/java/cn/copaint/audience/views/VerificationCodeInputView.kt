package cn.copaint.audience.views

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.* // ktlint-disable no-wildcard-imports
import cn.copaint.audience.R
import cn.copaint.audience.utils.SoftInputUtils
import cn.copaint.audience.utils.dp
import java.util.* // ktlint-disable no-wildcard-imports
import java.util.regex.Pattern

/**
 * Description : 验证码输入框
 *
 *
 * 支持粘贴功能
 *
 * @author WSoban
 * @date 2019/10/9
 */
interface OnInputListener {
    fun onComplete(code: String)
}

class VerificationCodeInputView : RelativeLayout {
    private var onInputListener: OnInputListener? = null
    private lateinit var mLinearLayout: LinearLayout
    private var mRelativeLayouts = mutableListOf<RelativeLayout>()
    private var mTextViews = mutableListOf<TextView>()
    private var mUnderLineViews = mutableListOf<View>()
    private var mCursorViews = mutableListOf<View>()
    private lateinit var mEditText: EditText
    private lateinit var mPopupWindow: PopupWindow
    private lateinit var valueAnimator: ValueAnimator
    private val mCodes = mutableListOf<String>()

    /**
     * 输入框数量
     */
    private var mEtNumber = 0

    /**
     * 输入框类型
     */
    private var mEtInputType: VCInputType? = null

    /**
     * 输入框的宽度
     */
    private var mEtWidth = 0

    /**
     * 输入框的高度
     */
    private var mEtHeight = 0

    /**
     * 文字颜色
     */
    private var mEtTextColor = 0

    /**
     * 文字大小
     */
    private var mEtTextSize = 0f

    /**
     * 输入框间距
     */
    private var mEtSpacing = 0

    /**
     * 平分后的间距
     */
    private var mEtBisectSpacing = 0

    /**
     * 判断是否平分,默认平分
     */
    private var isBisect = false

    /**
     * 输入框宽度
     */
    private var mViewWidth = 0

    /**
     * 下划线默认颜色,焦点颜色,高度,是否展示
     */
    private var mEtUnderLineDefaultColor = 0
    private var mEtUnderLineFocusColor = 0
    private var mEtUnderLineHeight = 0
    private var mEtUnderLineShow = false

    /**
     * 光标宽高,颜色
     */
    private var mEtCursorWidth = 0
    private var mEtCursorHeight = 0
    private var mEtCursorColor = 0

    /**
     * 输入框的背景色、焦点背景色、是否有焦点背景色
     */
    private var mEtBackground = 0
    private var mEtFocusBackground = 0
    private var isFocusBackgroud = false

    enum class VCInputType {
        /**
         * 数字类型
         */
        NUMBER,

        /**
         * 数字密码
         */
        NUMBERPASSWORD,

        /**
         * 文字
         */
        TEXT,
    }

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.VerificationCodeInputView)
        mEtNumber = typedArray.getInteger(R.styleable.VerificationCodeInputView_vive_et_number, 4)
        mEtWidth = typedArray.getDimensionPixelSize(R.styleable.VerificationCodeInputView_vive_et_width, 48.dp)
        mEtHeight = typedArray.getDimensionPixelSize(R.styleable.VerificationCodeInputView_vive_et_height, 48.dp)
        mEtTextColor = typedArray.getColor(R.styleable.VerificationCodeInputView_vive_et_text_color, Color.BLACK)
        mEtTextSize = typedArray.getDimensionPixelSize(R.styleable.VerificationCodeInputView_vive_et_text_size, 48.dp).toFloat()
        mEtBackground = typedArray.getResourceId(R.styleable.VerificationCodeInputView_vive_et_background, -1)
        if (mEtBackground < 0) mEtBackground = typedArray.getColor(R.styleable.VerificationCodeInputView_vive_et_background, Color.WHITE)
        isFocusBackgroud = typedArray.hasValue(R.styleable.VerificationCodeInputView_vive_et_focus_background)
        mEtFocusBackground = typedArray.getResourceId(R.styleable.VerificationCodeInputView_vive_et_focus_background, -1)
        if (mEtFocusBackground < 0) mEtFocusBackground = typedArray.getColor(R.styleable.VerificationCodeInputView_vive_et_focus_background, Color.WHITE)
        isBisect = typedArray.hasValue(R.styleable.VerificationCodeInputView_vive_et_spacing)
        if (isBisect) mEtSpacing = typedArray.getDimensionPixelSize(R.styleable.VerificationCodeInputView_vive_et_spacing, 0)
        mEtCursorWidth = typedArray.getDimensionPixelOffset(R.styleable.VerificationCodeInputView_vive_et_cursor_width, 2.dp)
        mEtCursorHeight = typedArray.getDimensionPixelOffset(R.styleable.VerificationCodeInputView_vive_et_cursor_height, 30.dp)
        mEtCursorColor = typedArray.getColor(R.styleable.VerificationCodeInputView_vive_et_cursor_color, Color.parseColor("#C3C3C3"))
        mEtUnderLineHeight = typedArray.getDimensionPixelOffset(R.styleable.VerificationCodeInputView_vive_et_underline_height, 1.dp)
        mEtUnderLineDefaultColor = typedArray.getColor(R.styleable.VerificationCodeInputView_vive_et_underline_default_color, Color.parseColor("#F0F0F0"))
        mEtUnderLineFocusColor = typedArray.getColor(R.styleable.VerificationCodeInputView_vive_et_underline_focus_color, Color.parseColor("#C3C3C3"))
        mEtUnderLineShow = typedArray.getBoolean(R.styleable.VerificationCodeInputView_vive_et_underline_show, false)
        initView()
        typedArray.recycle()
    }

    private fun initView() {
        mLinearLayout = LinearLayout(context)
        mLinearLayout.orientation = LinearLayout.HORIZONTAL
        mLinearLayout.gravity = Gravity.CENTER_HORIZONTAL
        mLinearLayout.layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        for (i in 0 until mEtNumber) {
            val relativeLayout = RelativeLayout(context)
            relativeLayout.layoutParams = getEtLayoutParams(i)
            setEtBackground(relativeLayout, mEtBackground)
            mRelativeLayouts.add(relativeLayout)
            val textView = TextView(context)
            initTextView(textView)
            relativeLayout.addView(textView)
            mTextViews.add(textView)
            val cursorView = View(context)
            initCursorView(cursorView)
            relativeLayout.addView(cursorView)
            mCursorViews.add(cursorView)
            if (mEtUnderLineShow) {
                val underLineView = View(context)
                initUnderLineView(underLineView)
                relativeLayout.addView(underLineView)
                mUnderLineViews.add(underLineView)
            }
            mLinearLayout.addView(relativeLayout)
        }
        addView(mLinearLayout)
        mEditText = EditText(context)
        initEdittext(mEditText)
        addView(mEditText)
        setCursorColor()
    }

    private fun initTextView(textView: TextView) {
        textView.layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        textView.textAlignment = TEXT_ALIGNMENT_CENTER
        textView.gravity = Gravity.CENTER
        textView.setTextColor(mEtTextColor)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mEtTextSize)
        setInputType(textView)
        textView.setPadding(0, 0, 0, 0)
    }

    private fun initCursorView(view: View) {
        val layoutParams = LayoutParams(mEtCursorWidth, mEtCursorHeight)
        layoutParams.addRule(CENTER_IN_PARENT)
        view.layoutParams = layoutParams
    }

    private fun initUnderLineView(view: View) {
        val layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mEtUnderLineHeight)
        layoutParams.addRule(ALIGN_PARENT_BOTTOM)
        view.layoutParams = layoutParams
        view.setBackgroundColor(mEtUnderLineDefaultColor)
    }

    private fun initEdittext(editText: EditText) {
        val layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams.addRule(ALIGN_TOP, mLinearLayout.id)
        layoutParams.addRule(ALIGN_BOTTOM, mLinearLayout.id)
        editText.layoutParams = layoutParams
        setInputType(editText)
        editText.setBackgroundColor(Color.TRANSPARENT)
        editText.setTextColor(Color.TRANSPARENT)
        editText.isCursorVisible = false
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(editable: Editable) {
                if (editable.isNotEmpty()) {
                    mEditText.setText("")
                    code = editable.toString()
                }
            }
        })
        // 监听验证码删除按键
        editText.setOnKeyListener { _: View, keyCode: Int, keyEvent: KeyEvent ->
            if (keyCode == KeyEvent.KEYCODE_DEL && keyEvent.action == KeyEvent.ACTION_DOWN && mCodes.size > 0) {
                mCodes.removeAt(mCodes.size - 1)
                showCode()
                return@setOnKeyListener true
            }
            false
        }
        editText.setOnLongClickListener {
            showPaste()
            false
        }
        getEtFocus(editText)
    }

    private fun initPopupWindow() {
        mPopupWindow =
            PopupWindow(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val tv = TextView(context)
        tv.text = "粘贴"
        tv.textSize = 14.0f
        tv.setTextColor(Color.BLACK)
        tv.setBackgroundResource(R.drawable.vciv_paste_bg)
        tv.setPadding(30, 10, 30, 10)
        tv.setOnClickListener { v: View? ->
            code = clipboardString
            mPopupWindow.dismiss()
        }
        mPopupWindow.contentView = tv
        mPopupWindow.width = LinearLayout.LayoutParams.WRAP_CONTENT // 设置菜单的宽度
        mPopupWindow.height = LinearLayout.LayoutParams.WRAP_CONTENT
        mPopupWindow.isFocusable = true // 获取焦点
        mPopupWindow.isTouchable = true // 设置PopupWindow可触摸
        mPopupWindow.isOutsideTouchable = true // 设置非PopupWindow区域可触摸
        // 设置点击隐藏popWindow
        val dw = ColorDrawable(Color.TRANSPARENT)
        mPopupWindow.setBackgroundDrawable(dw)
    }

    private fun setEtBackground(rl: RelativeLayout, background: Int) {
        if (background > 0) rl.setBackgroundResource(background)
        else rl.setBackgroundColor(background)
    }

    // 获取剪贴板中第一条数据
    private val clipboardString: String
        get() {
            val clipboardManager =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            // 获取剪贴板中第一条数据
            if (clipboardManager.hasPrimaryClip() && clipboardManager.primaryClipDescription!!.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                val itemAt = clipboardManager.primaryClip!!.getItemAt(0)
                if (!(itemAt == null || TextUtils.isEmpty(itemAt.text)))return itemAt.text.toString()
            }
            return ""
        }

    private fun getEtLayoutParams(i: Int): LinearLayout.LayoutParams {
        val layoutParams = LinearLayout.LayoutParams(mEtWidth, mEtHeight)
        var spacing: Int
        if (!isBisect) {
            spacing = mEtBisectSpacing / 2
        } else {
            spacing = mEtSpacing / 2
            // 如果大于最大平分数，将设为最大值
            if (mEtSpacing > mEtBisectSpacing) spacing = mEtBisectSpacing / 2
        }
        when (i) {
            0 -> {
                layoutParams.leftMargin = 0
                layoutParams.rightMargin = spacing
            }
            mEtNumber - 1 -> {
                layoutParams.leftMargin = spacing
                layoutParams.rightMargin = 0
            }
            else -> {
                layoutParams.leftMargin = spacing
                layoutParams.rightMargin = spacing
            }
        }
        return layoutParams
    }

    private fun setInputType(textView: TextView) {
        when (mEtInputType) {
            VCInputType.TEXT -> textView.inputType = InputType.TYPE_CLASS_TEXT
            else -> textView.inputType = InputType.TYPE_CLASS_NUMBER
        }
    }

    /**
     * 展示自定义的粘贴板
     */
    private fun showPaste() {
        // 去除输入框为数字模式，但粘贴板不是数字模式
        if ((mEtInputType == VCInputType.NUMBER || mEtInputType == VCInputType.NUMBERPASSWORD) && !isNumeric(clipboardString)) return
        if (!TextUtils.isEmpty(clipboardString)) {
            if (this::mPopupWindow.isInitialized) initPopupWindow()
            mPopupWindow.showAsDropDown(mTextViews[0], 0, 20)
            SoftInputUtils.hideSoftInput(context as Activity)
        }
    }

    /**
     * 判断粘贴板上的是不是数字
     *
     * @param str
     * @return
     */
    private fun isNumeric(str: String): Boolean {
        if (TextUtils.isEmpty(str)) return false
        val pattern = Pattern.compile("[0-9]*")
        val isNum = pattern.matcher(str)
        return isNum.matches()
    }

    private fun showCode() {
        for (i in 0 until mEtNumber) {
            val textView = mTextViews[i]
            if (mCodes.size > i) textView.text = mCodes[i]
            else textView.text = ""
        }
        setCursorColor() // 设置高亮跟光标颜色
        setCallBack() // 回调
    }

    /**
     * 设置焦点输入框底部线、光标颜色、背景色
     */
    private fun setCursorColor() {
        if (this::valueAnimator.isInitialized) valueAnimator.cancel()
        for (i in 0 until mEtNumber) {
            val cursorView = mCursorViews[i]
            cursorView.setBackgroundColor(Color.TRANSPARENT)
            if (mEtUnderLineShow) {
                val underLineView = mUnderLineViews[i]
                underLineView.setBackgroundColor(mEtUnderLineDefaultColor)
            }
            if (isFocusBackgroud) setEtBackground(mRelativeLayouts[i], mEtBackground)
        }
        if (mCodes.size < mEtNumber) {
            setCursorView(mCursorViews[mCodes.size])
            if (mEtUnderLineShow) mUnderLineViews[mCodes.size].setBackgroundColor(mEtUnderLineFocusColor)
            if (isFocusBackgroud) setEtBackground(mRelativeLayouts[mCodes.size], mEtFocusBackground)
        }
    }

    /**
     * 设置焦点色变换动画
     *
     * @param view
     */
    private fun setCursorView(view: View?) {
        valueAnimator = ObjectAnimator.ofInt(view, "backgroundColor", mEtCursorColor, android.R.color.transparent)
        valueAnimator.duration = 1500
        valueAnimator.repeatCount = -1
        valueAnimator.repeatMode = ValueAnimator.RESTART
        valueAnimator.setEvaluator { fraction: Float, startValue: Any?, endValue: Any? -> if (fraction <= 0.5f) startValue else endValue }
        valueAnimator.start()
    }

    private fun setCallBack() {
        if (onInputListener == null) return
        if (mCodes.size == mEtNumber) onInputListener!!.onComplete(code)
    }

    /**
     * 获得验证码
     *
     * @return 验证码
     */
    private var code: String
        get() {
            val sb = StringBuilder()
            for (code in mCodes) sb.append(code)
            return sb.toString()
        }
        private set(code) {
            if (TextUtils.isEmpty(code)) return
            for (char in code) if (mCodes.size < mEtNumber) mCodes.add(char.toString())
            showCode()
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mViewWidth = measuredWidth
        updateETMargin()
    }

    private fun updateETMargin() {
        // 平分Margin，把第一个TextView跟最后一个TextView的间距同设为平分
        mEtBisectSpacing = (mViewWidth - mEtNumber * mEtWidth) / (mEtNumber - 1)
        for (i in 0 until mEtNumber) mLinearLayout.getChildAt(i).layoutParams = getEtLayoutParams(i)
    }

    private fun getEtFocus(editText: EditText) {
        editText.isFocusable = true
        editText.isFocusableInTouchMode = true
        editText.requestFocus()
        SoftInputUtils.showSoftInput(context, editText)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        SoftInputUtils.hideSoftInput(context as Activity)
        if (this::valueAnimator.isInitialized) valueAnimator.cancel()
    }

    fun setOnInputListener(onInputListener: OnInputListener) {
        this.onInputListener = onInputListener
    }
}
