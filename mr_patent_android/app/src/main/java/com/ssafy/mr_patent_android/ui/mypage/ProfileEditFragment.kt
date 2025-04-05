package com.ssafy.mr_patent_android.ui.mypage

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.base.ApplicationClass.Companion.sharedPreferences
import com.ssafy.mr_patent_android.base.BaseFragment
import com.ssafy.mr_patent_android.data.model.dto.ProfileEditRequest
import com.ssafy.mr_patent_android.data.model.dto.UserDto
import com.ssafy.mr_patent_android.databinding.FragmentProfileEditBinding
import com.ssafy.mr_patent_android.ui.address.AddressViewModel
import com.ssafy.mr_patent_android.util.FileUtil
import com.ssafy.mr_patent_android.util.ImagePicker

private const val TAG = "ProfileEditFragment_Mr_Patent"
class ProfileEditFragment : BaseFragment<FragmentProfileEditBinding>(
    FragmentProfileEditBinding::bind, R.layout.fragment_profile_edit
) {
    private var isExpanded = mutableListOf(false, false, false, false, false)
    private val args: ProfileEditFragmentArgs by navArgs()

    private lateinit var imagePickerUtil: ImagePicker
    private lateinit var imageUri: Uri

    private val profileEditViewModel : ProfileEditViewModel by activityViewModels()
    private val addressViewModel : AddressViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initObserver()
    }

    private fun initView() {
        imagePickerUtil = ImagePicker(this) { uri ->
            val fileSize = FileUtil().getFileSize(requireContext(), uri)
            profileEditViewModel.setCurrentImage(uri.toString())

            if(fileSize >= 1024 * 1024 * 5) {
                setDialogSizeOver()
                return@ImagePicker
            }

            Glide.with(requireContext())
                .load(uri)
                .fallback(R.drawable.user_profile)
                .error(R.drawable.image_load_error_icon)
                .into(binding.ivProfile)
        }

        binding.tvBefore.setOnClickListener {
            findNavController().popBackStack()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        })

        when (args.role) {
            "member" -> {
                profileEditViewModel.getMemberInfo()
            }
            "expert" -> {
                profileEditViewModel.getExpertInfo(args.id)
            }
        }

        binding.tvEditProfile.setOnClickListener {
            imagePickerUtil.checkPermissionAndOpenGallery()
        }

        binding.clProfileEditCategoryExpert.setOnClickListener {
            toggleLayout(isExpanded[0], binding.ivToggle1, binding.hsvFilter)
            isExpanded[0] = !isExpanded[0]
        }

        binding.clProfileEditDescriptionExpert.setOnClickListener {
            toggleLayout(isExpanded[1], binding.ivToggle2, binding.etDescription)
            isExpanded[1] = !isExpanded[1]
        }

        binding.clProfileEditAddressExpert.setOnClickListener {
            toggleLayout(isExpanded[2], binding.ivToggle3, binding.clEtAddress)
            isExpanded[2] = !isExpanded[2]
        }

        binding.clProfileEditPhoneExpert.setOnClickListener {
            toggleLayout(isExpanded[3], binding.ivToggle4, binding.etPhone)
            isExpanded[3] = !isExpanded[3]
        }

        binding.clProfileEditItemsMember.setOnClickListener {
            toggleLayout(isExpanded[4], binding.ivToggle, binding.etName)
            isExpanded[4] = !isExpanded[4]
        }

        binding.ivSearch.setOnClickListener {
            findNavController().navigate(R.id.addressSearchFragment)
        }

        binding.cgFilter.setOnCheckedStateChangeListener { group, index ->
            if (index.isEmpty()) {
                group.check(group.tag as? Int ?: R.id.chip_chemi)
            } else {
                group.tag = index.first()
            }
        }

        binding.btnEdit.setOnClickListener {
            var profileEditRequest = ProfileEditRequest(null, null, null, null, null, null)

            if (profileEditViewModel.profileImage.value != profileEditViewModel.currentImage.value) {
                profileEditRequest.userImage = profileEditViewModel.currentImage.value.toString()
            }

            when (args.role) {
                "member" -> {
                    if (binding.etName.text.toString() != profileEditViewModel.memberInfo.value?.userName) {
                        if (binding.etName.text.isNotBlank() && binding.etName.text.toString().length in (3..12)) {
                            profileEditRequest.userName = binding.etName.text.toString()
                        } else {
                            showCustomToast("이름이 형식에 맞지 않습니다.")
                        }
                    }
                }

                "expert" -> {
                    if (binding.etDescription.text.toString() != profileEditViewModel.expertInfo.value?.expertDescription) {
                        if (binding.etDescription.text.isNotBlank() && binding.etDescription.text.toString().length in (1..500)) {
                            profileEditRequest.expertDescription = binding.etDescription.text.toString()
                        } else {
                            showCustomToast("소개글이 형식에 맞지 않습니다.")
                        }
                    }

                    if (binding.etPhone.text.toString() != profileEditViewModel.expertInfo.value?.expertPhone) {
                        if (binding.etPhone.text.isNotBlank() && binding.etPhone.toString().length in (9..11)) {
                            profileEditRequest.expertPhone = binding.etPhone.text.toString()
                        } else {
                            showCustomToast("전화번호가 형식에 맞지 않습니다.")
                        }
                    }

                    val addressChanged1 = binding.etAddress1.text.toString() != profileEditViewModel.expertInfo.value?.expertAddress?.substringBefore("\\")
                    val addressChanged2 = binding.etAddress2.text.toString() != profileEditViewModel.expertInfo.value?.expertAddress?.substringAfter("\\")

                    if (addressChanged1 || addressChanged2) {
                        val address = binding.etAddress1.text.toString() + "\\" + binding.etAddress2.text.toString()
                        profileEditRequest.expertAddress = address
                    }

                    val newCategories = mutableListOf<ProfileEditRequest.Category>()
                    for (i in 0 until binding.cgFilter.childCount) {
                        val chip = binding.cgFilter.getChildAt(i) as Chip
                        if (chip.isChecked) {
                            val category = ProfileEditRequest.Category(chip.text.toString())
                            newCategories.add(category)
                        }
                    }

                    if (newCategories.toSet() != profileEditViewModel.expertInfo.value?.expertCategory?.toSet()) {
                        profileEditRequest.expertCategories = newCategories
                    }
                }
            }

            val isEdited = profileEditRequest.userName != null
                    || profileEditRequest.userImage != null
                    || profileEditRequest.expertDescription != null
                    || profileEditRequest.expertAddress != null
                    || profileEditRequest.expertPhone != null
                    || profileEditRequest.expertCategories != null

            Log.d(TAG, "initView: ${isEdited}")
            Log.d(TAG, "initView: name ${profileEditRequest.userName}")
            Log.d(TAG, "initView: address ${profileEditRequest.expertAddress}")
            Log.d(TAG, "initView: image ${profileEditRequest.userImage}")
            Log.d(TAG, "initView: phone ${profileEditRequest.expertPhone}")
            Log.d(TAG, "initView: description ${profileEditRequest.expertDescription}")
            Log.d(TAG, "initView: categories ${profileEditRequest.expertCategories}")

            if (isEdited) {
                profileEditViewModel.editUserInfo(profileEditRequest)
            }
        }
    }

    fun toggleLayout(isExpanded: Boolean, view: View, layout: View) {
        if (isExpanded) {
            view.rotation = 180f
            layout.visibility = View.VISIBLE
        } else {
            view.rotation = 0f
            layout.visibility = View.GONE
        }
    }

    private fun initObserver() {
        profileEditViewModel.toastMsg.observe(viewLifecycleOwner) {
            if (it == "회원정보가 성공적으로 수정되었습니다.") {
                showCustomToast(it)
                profileEditViewModel.clearToastMsg()
            }
        }

        profileEditViewModel.expertInfo.observe(viewLifecycleOwner) {
            binding.clProfileEditItemsMember.visibility = View.GONE
            binding.clProfileEditItemsExpert.visibility = View.VISIBLE
            binding.etDescription.setText(it.expertDescription)
            binding.etPhone.setText(it.expertPhone)
            binding.etAddress1.setText(it.expertAddress.substringBefore("\\"))
            binding.etAddress2.setText(it.expertAddress.substringAfter("\\"))
            for (category in it.expertCategory) {
                when (category) {
                    "화학공학" -> binding.chipChemi.isChecked = true
                    "기계공학" -> binding.chipMecha.isChecked = true
                    "생명공학" -> binding.chipLife.isChecked = true
                    "전기/전자" -> binding.chipElec.isChecked = true
                }
            }
        }

        profileEditViewModel.memberInfo.observe(viewLifecycleOwner) {
            binding.clProfileEditItemsMember.visibility = View.VISIBLE
            binding.clProfileEditItemsExpert.visibility = View.GONE
            binding.etName.setText(it.userName)
        }

        profileEditViewModel.profileImage.observe(viewLifecycleOwner, {
            if (it.isNotBlank()) {
                imageUri = Uri.parse(it)
                Glide.with(requireContext())
                    .load(imageUri)
                    .fallback(R.drawable.user_profile)
                    .error(R.drawable.image_load_error_icon)
                    .into(binding.ivProfile)
            } else {
                imageUri = Uri.parse(requireContext().resources.getString(R.string.default_image))
            }
            profileEditViewModel.setCurrentImage(imageUri.toString())
        })

        addressViewModel.address.observe(viewLifecycleOwner) {
            binding.etAddress1.setText(it)
            toggleLayout(true, binding.ivToggle3, binding.clEtAddress)
            isExpanded[2] = false
        }
    }

    private fun setDialogSizeOver() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_size_over, null)
        val dialogBuilder = Dialog(requireContext())
        dialogBuilder.setContentView(dialogView)
        dialogBuilder.create()
        dialogBuilder.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(
                ((context.resources.displayMetrics.widthPixels) * 0.8).toInt(),
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        }
        dialogBuilder.show()

        val dlBtnYes = dialogView.findViewById<Button>(R.id.dl_btn_yes)

        dlBtnYes.setOnClickListener {
            dialogBuilder.dismiss()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(key: String, value: String) =
            ProfileEditFragment().apply {
                arguments = Bundle().apply {
                    putString(key, value)
                }
            }
    }
}