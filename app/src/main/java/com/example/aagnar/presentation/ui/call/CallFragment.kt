package com.example.aagnar.presentation.ui.call

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.aagnar.R
import com.example.aagnar.presentation.viewmodel.CallViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CallFragment : Fragment() {

    private val viewModel: CallViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_call, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        println("CallFragment - READY FOR MATRIX CALLS")

        // 🔥 ПЕРЕКЛЮЧАЕМ НА MATRIX ЗВОНКИ:
        view.findViewById<View>(R.id.btnMakeCall)?.setOnClickListener {
            // ВМЕСТО SIP: viewModel.makeCall("sip:...", false)
            // БУДЕТ: viewModel.startMatrixCall("user_id", false)
            println("Make Matrix call clicked!")
        }

        view.findViewById<View>(R.id.btnEndCall)?.setOnClickListener {
            // БУДЕТ: viewModel.endMatrixCall()
            println("End Matrix call clicked!")
        }
    }
}