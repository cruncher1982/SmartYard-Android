package com.sesameware.smartyard_oem.screen

import android.view.View
import com.kaspersky.kaspresso.screens.KScreen
import com.sesameware.smartyard_oem.R
import com.sesameware.smartyard_oem.ui.main.MainActivity
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KButton
import org.hamcrest.Matcher

/**
 * @author Nail Shakurov
 * Created on 01.08.2020.
 */
object AddressScreen : KScreen<AddressScreen>() {
    override val layoutId: Int? = R.layout.fragment_address
    override val viewClass: Class<*>? = MainActivity::class.java
    val rv_parent = KRecyclerView(
        builder = { withId(R.id.addressList) },
        itemTypeBuilder = {
            itemType(::Item)
        }
    )
    class Item(parent: Matcher<View>) :
        KRecyclerItem<Item>(parent) {
        val tbOpen = KButton(parent) { withId(R.id.tbOpen) }
    }
}
