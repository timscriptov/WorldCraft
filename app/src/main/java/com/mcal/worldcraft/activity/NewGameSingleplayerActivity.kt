package com.mcal.worldcraft.activity

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.mcal.worldcraft.R
import com.mcal.worldcraft.databinding.ActivityNewGameSinglePlayerBinding
import com.mcal.worldcraft.utils.GameStarter
import com.mcal.worldcraft.utils.KeyboardUtils
import com.mcal.worldcraft.utils.WorldGenerator

class NewGameSinglePlayerActivity : BaseActivity() {
    private lateinit var binding: ActivityNewGameSinglePlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewGameSinglePlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val worldNameView = binding.worldNameEditText
        KeyboardUtils.hideKeyboardOnEnter(this, worldNameView)

        val mapTypeSpinner = binding.mapTypeSpinner
        initMapTypeDropDownMenu(mapTypeSpinner)

        val worldTypeView = binding.worldType
        initGameMode(worldTypeView)

        binding.backButton.setOnClickListener { onBackPressed() }

        binding.startButton.setOnClickListener {
            KeyboardUtils.hideKeyboard(this, worldNameView)
            GameStarter.startGame(
                this,
                worldNameView.text.toString(),
                true,
                mapTypeSpinner.selectedItemPosition,
                if (worldTypeView.selectedItemPosition == 0) WorldGenerator.Mode.CREATIVE else WorldGenerator.Mode.SURVIVAL
            )
            finishActivityAndCloseParent()
        }
        window.setSoftInputMode(2)
    }

    private fun finishActivityAndCloseParent() {
        setResult(RESULT_OK, Intent().apply {
            putExtra(SinglePlayerActivity.SHOULD_FINISH, true)
        })
        finish()
    }

    private fun initMapTypeDropDownMenu(dropDownMenu: Spinner) {
        ArrayAdapter(
            this,
            R.layout.spinner_item,
            arrayListOf(getString(R.string.random_map), getString(R.string.flat_map))
        ).apply {
            setDropDownViewResource(R.layout.spinner_dropdown_item)
        }.also { dropDownMenu.adapter = it }
    }

    private fun initGameMode(dropDownMenu: Spinner) {
        ArrayAdapter(
            this,
            R.layout.spinner_item,
            arrayListOf("Creative", "Survival")
        ).apply {
            setDropDownViewResource(R.layout.spinner_dropdown_item)
        }.also { dropDownMenu.adapter = it }
    }
}