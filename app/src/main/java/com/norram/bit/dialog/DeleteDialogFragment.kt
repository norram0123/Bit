package com.norram.bit

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class DeleteDialogFragment(
    private val userAdapter: UserAdapter,
    private val historyList: ArrayList<HashMap<String, String>>,
    private val position: Int
    ): DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            builder.setMessage(R.string.dialog_delete_history)
                .setPositiveButton(R.string.ok) { _, _ ->
                    val helper = HistoryOpenHelper(requireContext())
                    helper.writableDatabase.use { db ->
                        db.execSQL("DELETE FROM HISTORY_TABLE WHERE id = (SELECT id FROM HISTORY_TABLE ORDER BY id DESC LIMIT 1 OFFSET '$position')")
                    }
                    historyList.removeAt(position)
                    userAdapter.notifyItemRemoved(position)
                    userAdapter.notifyItemRangeChanged(position+1, historyList.size - position)
                }
                .setNegativeButton(R.string.cancel) { _, _ -> }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
