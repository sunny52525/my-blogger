package com.shaun.myblogger.Fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.shaun.myblogger.R
import com.shaun.myblogger.databinding.FragmentAboutBinding

class FragmentAbout : Fragment() {
    private lateinit var binding: FragmentAboutBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        container!!.removeAllViews()

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_about, container, false)

        binding.imgEmail.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.ic_gmail_night,
            0,
            0,
            0
        )
        binding.imgGithub.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.ic_github_night,
            0,
            0,
            0
        )
        binding.imgInstagram.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.ic_twitter,
            0,
            0,
            0
        )


        binding.imgGithub.setOnClickListener {
            val gitIntent = Intent(Intent.ACTION_VIEW)
            gitIntent.data = Uri.parse("http://github.com/sunny52525")
            startActivity(gitIntent)
        }

        binding.imgInstagram.setOnClickListener {
            val twitterIntent = Intent(Intent.ACTION_VIEW)
            twitterIntent.data = Uri.parse("https://www.twitter.com/sunny52525/")
            startActivity(twitterIntent)
        }

        binding.imgEmail.setOnClickListener {
            val mailIntent = Intent().apply {
                action = Intent.ACTION_SENDTO
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf("kumarsunny3232@gmail.com"))
                putExtra(Intent.EXTRA_SUBJECT, "App: My Blogger ")
            }
            startActivity(mailIntent)
        }
        return binding.root
    }

}