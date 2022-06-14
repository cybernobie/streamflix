package com.tanasi.navigation.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.FrameLayout
import androidx.appcompat.view.SupportMenuInflater
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.content.res.getResourceIdOrThrow
import com.tanasi.navigation.R

@SuppressLint("RestrictedApi")
class NavigationSlideView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attrs, defStyle) {

    val menu = NavigationSlideMenu(context)
    private val menuView = NavigationSlideMenuView(context)
    private val presenter = NavigationSlidePresenter()
    private val menuInflater: MenuInflater = SupportMenuInflater(context)

    private var selectedListener: ((item: MenuItem) -> Boolean)? = null
    private var reselectedListener: ((item: MenuItem) -> Boolean)? = null

    /**
     * Currently selected menu item ID, or zero if there is no menu.
     */
    var selectedItemId: Int
        get() = menuView.selectedItemId
        set(value) {
            val item = menu.findItem(value)
            if (item != null) {
                if (!menu.performItemAction(item, presenter, 0)) {
                    item.isChecked = true
                }
            }
        }

    /**
     * Current gravity setting for how destinations in the menu view will be grouped.
     */
    private var menuGravity: Int
        get() = menuView.menuGravity
        set(value) {
            menuView.menuGravity = value
        }


    init {
        val attributes = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.NavigationSlideView,
            0,
            0
        )

        presenter.menuView = menuView
        menuView.presenter = presenter
        menu.addMenuPresenter(presenter)
        presenter.initForMenu(getContext(), menu)

        menuGravity = attributes.getInt(
            R.styleable.NavigationSlideView_menuGravity,
            DEFAULT_MENU_GRAVITY
        )

        inflateMenu(attributes.getResourceIdOrThrow(R.styleable.NavigationSlideView_menu))

        attributes.recycle()

        addView(menuView)

        menu.setCallback(
            object : MenuBuilder.Callback {
                override fun onMenuItemSelected(menu: MenuBuilder, item: MenuItem): Boolean {
                    if (reselectedListener != null && item.itemId == selectedItemId) {
                        reselectedListener?.invoke(item)
                        return true
                    }
                    return !(selectedListener?.invoke(item) ?: true)
                }

                override fun onMenuModeChange(menu: MenuBuilder) {}
            })
    }


    fun setOnItemSelectedListener(onNavigationItemSelected: (item: MenuItem) -> Boolean) {
        selectedListener = onNavigationItemSelected
    }

    fun setOnItemReselectedListener(onNavigationItemReselected: (item: MenuItem) -> Boolean) {
        reselectedListener = onNavigationItemReselected
    }

    /**
     * Inflate a menu resource into this navigation view.
     *
     *
     * Existing items in the menu will not be modified or removed.
     *
     * @param resId ID of a menu resource to inflate
     */
    fun inflateMenu(resId: Int) {
        presenter.updateSuspended = true
        menuInflater.inflate(resId, menu)
        presenter.updateSuspended = false
        presenter.updateMenuView(true)
    }


    companion object {
        const val DEFAULT_MENU_GRAVITY = Gravity.TOP or Gravity.CENTER_HORIZONTAL
    }
}