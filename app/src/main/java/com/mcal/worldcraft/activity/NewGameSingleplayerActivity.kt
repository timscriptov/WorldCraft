package com.mcal.worldcraft.activity

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.mcal.worldcraft.R
import com.mcal.worldcraft.databinding.ActivityNewGameSingleplayerBinding
import com.mcal.worldcraft.utils.GameStarter
import com.mcal.worldcraft.utils.KeyboardUtils
import com.mcal.worldcraft.utils.WorldGenerator

class NewGameSingleplayerActivity : BaseActivity() {
    private lateinit var binding: ActivityNewGameSingleplayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewGameSingleplayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val worldNameView = binding.worldNameEditText
        KeyboardUtils.hideKeyboardOnEnter(this, worldNameView)

        val mapTypeSpinner = binding.mapTypeSpinner
        initMapTypeDropDownMenu(mapTypeSpinner)

        val worldTypeView = binding.worldType
        initGameMode(worldTypeView)

        binding.backButton.setOnClickListener { onBackPressed() }

        binding.startButton.setOnClickListener {
            KeyboardUtils.hideKeyboard(this@NewGameSingleplayerActivity, worldNameView)
            GameStarter.startGame(
                this@NewGameSingleplayerActivity,
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
        val intent = Intent()
        intent.putExtra(SinglePlayerActivity.SHOULD_FINISH, true)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun initMapTypeDropDownMenu(dropDownMenu: Spinner) {
        val list = ArrayList<String>()
        list.add(getString(R.string.random_map))
        list.add(getString(R.string.flat_map))
        ArrayAdapter(this, R.layout.spinner_item, list).apply {
            setDropDownViewResource(R.layout.spinner_dropdown_item)
        }.also { dropDownMenu.adapter = it }
    }

    private fun initGameMode(dropDownMenu: Spinner) {
        val list = ArrayList<String>()
        list.add("Creative")
        list.add("Survival")
        ArrayAdapter(this, R.layout.spinner_item, list).apply {
            setDropDownViewResource(R.layout.spinner_dropdown_item)
        }.also { dropDownMenu.adapter = it }
    }
}