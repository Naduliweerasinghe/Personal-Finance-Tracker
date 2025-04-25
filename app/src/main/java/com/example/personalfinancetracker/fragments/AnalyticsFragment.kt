package com.example.personalfinancetracker.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.personalfinancetracker.R
import com.example.personalfinancetracker.models.Transaction
import com.example.personalfinancetracker.utils.DateUtils
import com.example.personalfinancetracker.utils.PreferencesManager
import com.example.personalfinancetracker.viewmodels.TransactionViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Currency
import java.util.Date
import java.util.Locale

class AnalyticsFragment : Fragment() {

    private lateinit var textViewMonthYear: TextView
    private lateinit var textViewTotalExpense: TextView
    private lateinit var recyclerViewCategories: RecyclerView
    private lateinit var textViewEmptyState: TextView
    private lateinit var pieChart: PieChart
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var categoryExpenseAdapter: CategoryExpenseAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_analytics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferencesManager = PreferencesManager(requireContext())
        transactionViewModel = ViewModelProvider(this)[TransactionViewModel::class.java]

        initViews(view)
        setupRecyclerView()
        setupPieChart()
        setupObservers()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            transactionViewModel.getAllTransactions().collectLatest { transactions ->
                loadAnalytics(transactions)
            }
        }
    }

    private fun initViews(view: View) {
        textViewMonthYear = view.findViewById(R.id.textViewMonthYear)
        textViewTotalExpense = view.findViewById(R.id.textViewTotalExpense)
        recyclerViewCategories = view.findViewById(R.id.recyclerViewCategories)
        textViewEmptyState = view.findViewById(R.id.textViewEmptyState)
        pieChart = view.findViewById(R.id.pieChart)
    }

    private fun setupRecyclerView() {
        recyclerViewCategories.layoutManager = LinearLayoutManager(requireContext())
        categoryExpenseAdapter = CategoryExpenseAdapter(emptyList(), preferencesManager.getCurrency())
        recyclerViewCategories.adapter = categoryExpenseAdapter
    }

    private fun setupPieChart() {
        pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            holeRadius = 58f
            transparentCircleRadius = 61f
            setDrawCenterText(true)
            setCenterText("Expenses")
            setCenterTextSize(16f)
            setCenterTextColor(Color.WHITE)
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            animateY(1400, Easing.EaseInOutQuad)
            legend.isEnabled = false
            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(12f)
        }
    }

    private fun loadAnalytics(transactions: List<Transaction>) {
        val currentMonthTransactions = transactions.filter { transaction: Transaction -> DateUtils.isCurrentMonth(transaction.date) }

        // Set month and year
        val currentDate = Date()
        textViewMonthYear.text = DateUtils.formatMonthYear(currentDate)

        if (currentMonthTransactions.isEmpty()) {
            textViewEmptyState.visibility = View.VISIBLE
            recyclerViewCategories.visibility = View.GONE
            pieChart.visibility = View.GONE
            textViewTotalExpense.text = "No data available"
            categoryExpenseAdapter.updateData(emptyList())
            return
        }

        textViewEmptyState.visibility = View.GONE
        recyclerViewCategories.visibility = View.VISIBLE
        pieChart.visibility = View.VISIBLE

        // Calculate total expense
        val totalExpense = currentMonthTransactions.filter { transaction: Transaction -> transaction.isExpense }.sumOf { transaction: Transaction -> transaction.amount }
        val currency = preferencesManager.getCurrency()
        val numberFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
        numberFormat.currency = Currency.getInstance(currency)
        textViewTotalExpense.text = numberFormat.format(totalExpense)

        // Group expenses by category
        val expensesByCategory = currentMonthTransactions
            .filter { transaction: Transaction -> transaction.isExpense }
            .groupBy { transaction: Transaction -> transaction.category }
            .map { (category, transactions) ->
                val amount = transactions.sumOf { transaction: Transaction -> transaction.amount }
                val percentage = if (totalExpense > 0) (amount / totalExpense) * 100 else 0.0
                CategoryExpense(category, amount, percentage)
            }
            .sortedByDescending { categoryExpense: CategoryExpense -> categoryExpense.amount }

        // Update pie chart
        updatePieChart(expensesByCategory)

        // Update adapter with new data
        categoryExpenseAdapter.updateData(expensesByCategory)
    }

    private fun updatePieChart(categories: List<CategoryExpense>) {
        val entries = categories.map { PieEntry(it.percentage.toFloat(), it.category) }
        val dataSet = PieDataSet(entries, "").apply {
            colors = ColorTemplate.MATERIAL_COLORS.asList()
            valueTextColor = Color.WHITE
            valueTextSize = 12f
            valueFormatter = PercentFormatter(pieChart)
            setDrawValues(true)
            yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        }

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.invalidate()
    }

    private data class CategoryExpense(
        val category: String,
        val amount: Double,
        val percentage: Double
    )

    private inner class CategoryExpenseAdapter(
        private var categories: List<CategoryExpense>,
        private val currency: String
    ) : RecyclerView.Adapter<CategoryExpenseAdapter.ViewHolder>() {

        fun updateData(newCategories: List<CategoryExpense>) {
            categories = newCategories
            notifyDataSetChanged()
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textViewCategory: TextView = view.findViewById(R.id.textViewCategoryName)
            val textViewAmount: TextView = view.findViewById(R.id.textViewCategoryAmount)
            val textViewPercentage: TextView = view.findViewById(R.id.textViewCategoryPercentage)
            val progressBar: View = view.findViewById(R.id.viewCategoryProgress)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_category_expense, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val categoryExpense = categories[position]

            holder.textViewCategory.text = categoryExpense.category

            val numberFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
            numberFormat.currency = Currency.getInstance(currency)
            holder.textViewAmount.text = numberFormat.format(categoryExpense.amount)

            holder.textViewPercentage.text = String.format("%.1f%%", categoryExpense.percentage)

            val layoutParams = holder.progressBar.layoutParams
            layoutParams.width = (categoryExpense.percentage * holder.itemView.width / 100).toInt()
            holder.progressBar.layoutParams = layoutParams
        }

        override fun getItemCount() = categories.size
    }
}