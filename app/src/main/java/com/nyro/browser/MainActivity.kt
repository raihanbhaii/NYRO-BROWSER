menu.add(0, 1, 0, "New Tab").apply {
    setIcon(android.R.drawable.ic_menu_add)
}
menu.add(0, 2, 1, "Tabs").apply {
    setIcon(android.R.drawable.ic_menu_edit)
}
menu.add(0, 3, 2, "Settings").apply {
    setIcon(android.R.drawable.ic_menu_preferences)
}

setOnNavigationItemSelectedListener { item ->
    when (item.itemId) {
        1 -> createNewTab()
        2 -> showTabManager()
        3 -> showSettings()
    }
    true
}
