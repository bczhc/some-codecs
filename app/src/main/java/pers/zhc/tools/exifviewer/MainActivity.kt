package pers.zhc.tools.exifviewer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import pers.zhc.tools.BaseActivity
import pers.zhc.tools.MyApplication
import pers.zhc.tools.MyApplication.Companion.GSON
import pers.zhc.tools.R
import pers.zhc.tools.databinding.ExifViewerDetailedInfoDialogBinding
import pers.zhc.tools.databinding.ExifViewerEntryItemBinding
import pers.zhc.tools.databinding.ExifViewerMainBinding
import pers.zhc.tools.jni.JNI
import pers.zhc.tools.texteditor.ExifEntry
import pers.zhc.tools.utils.*

class MainActivity : BaseActivity() {
    private lateinit var bindings: ExifViewerMainBinding
    private var exifEntries = ExifEntries()
    private lateinit var listAdapter: ListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindings = ExifViewerMainBinding.inflate(layoutInflater)
        setContentView(bindings.root)

        listAdapter = ListAdapter(exifEntries)
        bindings.recyclerView.apply {
            addDividerLines()
            setLinearLayoutManager()
            setUpFastScroll(this@MainActivity)
            adapter = listAdapter
        }

        // launched from the "Open as" dialog
        if (intent.action == Intent.ACTION_VIEW) {
            // TODO: workaround; should use content provider but not direct path
            intent.data.nullMap { it.path }?.let { path ->
                bindings.tiet.setText(path)
                updateResult()
            }
        }

        bindings.readBtn.setOnClickListener {
            updateResult()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateResult() {
        runCatching {
            val path = bindings.tiet.text.toString()
            val exifInfo = JNI.Exif.getExifInfo(path)
            exifEntries.clear()
            exifEntries.addAll(GSON.fromJson(exifInfo, Array<ExifEntry>::class.java))
            listAdapter.notifyDataSetChanged()
        }.toastOnFailure(this)
    }

    class ListAdapter(val entries: ExifEntries) : RecyclerView.Adapter<ListAdapter.ViewHolder>() {
        class ViewHolder(bindings: ExifViewerEntryItemBinding) : RecyclerView.ViewHolder(bindings.root) {
            val tv = bindings.textView
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val bindings = ExifViewerEntryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(bindings)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val context = holder.tv.context
            val entry = entries[position]
            val text = "${entry.tagDesc}: ${entry.valueReadable}"
            holder.tv.text = text

            holder.tv.setOnClickListener {
                val bindings = ExifViewerDetailedInfoDialogBinding.inflate(LayoutInflater.from(context))
                bindings.apply {
                    tagIdTv.text = "0x${entry.tagId.toString(16).completeLeadingZeros(4)}"
                    tagTv.text = entry.tagDisplay
                    tagDescTv.text = entry.tagDesc
                    valueDisplayTv.text = entry.valueDisplay
                    valueReadableTv.text = entry.valueReadable
                    valueInternalTv.text = entry.valueInternal

                    tagLl.setOnLongClickListener {
                        ClipboardUtils.putWithToast(MyApplication.appContext, entry.tagDisplay)
                        true
                    }
                }

                MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.exif_detailed_info_dialog_title)
                    .setView(bindings.root)
                    .show()
            }
        }

        override fun getItemCount(): Int {
            return entries.size
        }
    }
}

typealias ExifEntries = ArrayList<ExifEntry>