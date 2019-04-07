package com.orsteg.wiconnmobileapp

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.transition.Slide
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import kotlinx.android.synthetic.main.activity_signup.*
import java.util.ArrayList

class SignUpActivity : AppCompatActivity() {

    private var mCurrentStep: Int = 0
    private var currentFragment: Fragment? = null
    private val stepCount = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        if (savedInstanceState!= null) mCurrentStep = savedInstanceState.getInt("current_step", 0)

        back.setOnClickListener{
            previousStep()
        }

        setStep()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.putInt("current_step", mCurrentStep)
    }

    override fun onBackPressed() {
        if (mCurrentStep > 0) {
            mCurrentStep--
            setStep(false)
        } else {
            super.onBackPressed()
        }

    }

    fun nextStep() {
        if (mCurrentStep < stepCount) {
            mCurrentStep++
            setStep()
        } else {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }


    fun previousStep() {
        if (mCurrentStep > 0) {
            mCurrentStep--
            setStep(false)
        } else {
            finish()
        }
    }

    fun setStep(foward: Boolean = true) {

        var isNull = false
        val fragment = supportFragmentManager.findFragmentByTag("fragment.signup.$mCurrentStep")
                ?: SignUpFragment.newInstance(mCurrentStep, "").apply { isNull = true }

        if (fragment != currentFragment) {

            val transaction = supportFragmentManager.beginTransaction()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                if (currentFragment != null && currentFragment!!.isVisible) {
                    (currentFragment as SignUpFragment).addSharedElements(transaction)
                    currentFragment?.exitTransition = Slide().apply {
                        duration = 400
                        interpolator = AccelerateDecelerateInterpolator()
                        slideEdge = if (foward) Gravity.START else Gravity.END
                    }
                }

                fragment.apply {
                    sharedElementEnterTransition = MainActivity.DetailsTransition().apply {
                        duration = 400
                        interpolator = AccelerateDecelerateInterpolator()
                    }
                    enterTransition = Slide().apply {
                        duration = 600
                        interpolator = AccelerateDecelerateInterpolator()
                        slideEdge = if (foward) Gravity.END else Gravity.START
                    }
                }
            }

            if (isNull) {
                transaction.replace(steps.id, fragment, "fragment.signup.$mCurrentStep")
            } else {
                transaction.replace(steps.id, fragment)
            }

            transaction.disallowAddToBackStack()

            transaction.commitAllowingStateLoss()
        }

        currentFragment = fragment

    }


    private fun hideInput(){
        val v = this.currentFocus
        if (v != null){
            val inp = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            inp?.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }


    private fun networkTest(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val activeNetworkInfo = connectivityManager?.activeNetworkInfo

        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
    }

    companion object {
        private val TAG = "MYTAG"
    }


    /**
     * A simple [Fragment] subclass.
     * Use the [SignUpFragment.newInstance] factory method to
     * create an instance of this fragment.
     */
    class SignUpFragment : Fragment() {

        private var mStep: Int? = null
        private var mSignUpInfo: String? = null

        private var cont: View? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            if (arguments != null) {
                mStep = arguments!!.getInt(ARG_STEP)
                mSignUpInfo = arguments!!.getString(ARG_INFO)
            }
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            // Inflate the layout for this fragment

            val view = when (mStep) {
                        0 -> inflater.inflate(R.layout.name_layout, container, false)
                        1 -> inflater.inflate(R.layout.gender_layout, container, false)
                        2 -> inflater.inflate(R.layout.number_layout, container, false)
                        3 -> inflater.inflate(R.layout.verify_layout, container, false)
                        4 -> inflater.inflate(R.layout.username_layout, container, false)
                        else -> null
                    }

            return view
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            cont = view.findViewById<View?>(R.id.cont)
            cont?.setOnClickListener {
                (context as? SignUpActivity)?.nextStep()
            }
        }

        fun addSharedElements(transaction: FragmentTransaction) {
            transaction.addSharedElement(cont,"signup_continue")
        }

        companion object {
            private val ARG_STEP = "param_step"
            private val ARG_INFO = "param_info"


            fun newInstance(param1: Int, param2: String): SignUpFragment {
                val fragment = SignUpFragment()
                val args = Bundle()
                args.putInt(ARG_STEP, param1)
                args.putString(ARG_INFO, param2)
                fragment.arguments = args
                return fragment
            }
        }

    }

}
