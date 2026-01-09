package com.example.lt1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lt1.ui.theme.LT1Theme
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LT1Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CalculatorScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun CalculatorScreen(modifier: Modifier = Modifier) {
    var displayValue by remember { mutableStateOf("0") }
    var previousValue by remember { mutableStateOf(0.0) }
    var operation by remember { mutableStateOf<Char?>(null) }
    var waitingForNewValue by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var memoryValue by remember { mutableStateOf(0.0) }
    var hasMemory by remember { mutableStateOf(false) }
    var history by remember { mutableStateOf<List<String>>(emptyList()) }
    
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp

    fun formatResult(result: Double): String {
        return if (result % 1.0 == 0.0) {
            result.toInt().toString()
        } else {
            // Ограничиваем количество знаков после запятой
            String.format("%.10f", result).trimEnd('0').trimEnd('.')
        }
    }

    fun handleNumberInput(number: String) {
        if (errorMessage != null) {
            errorMessage = null
        }
        if (waitingForNewValue) {
            displayValue = number
            waitingForNewValue = false
        } else {
            displayValue = if (displayValue == "0" || displayValue == "Ошибка: деление на 0") {
                number
            } else {
                displayValue + number
            }
        }
    }

    fun handleDecimalPoint() {
        if (errorMessage != null) {
            errorMessage = null
            displayValue = "0."
            waitingForNewValue = false
            return
        }
        if (waitingForNewValue) {
            displayValue = "0."
            waitingForNewValue = false
        } else if (!displayValue.contains(".")) {
            displayValue += "."
        }
    }

    fun handleSignChange() {
        if (errorMessage != null) {
            return
        }
        val currentValue = displayValue.toDoubleOrNull()
        if (currentValue != null) {
            displayValue = if (currentValue == 0.0) {
                "0"
            } else if (displayValue.startsWith("-")) {
                displayValue.substring(1)
            } else {
                "-$displayValue"
            }
        }
    }

    fun handleOperation(op: Char) {
        if (errorMessage != null) {
            return
        }
        val currentValue = displayValue.toDoubleOrNull() ?: 0.0
        
        if (operation != null && !waitingForNewValue) {
            val result = when (operation) {
                '+' -> previousValue + currentValue
                '-' -> previousValue - currentValue
                '*' -> previousValue * currentValue
                '/' -> if (currentValue != 0.0) previousValue / currentValue else Double.NaN
                else -> currentValue
            }
            
            if (result.isNaN()) {
                displayValue = "Ошибка: деление на 0"
                errorMessage = "Ошибка: деление на 0"
                previousValue = 0.0
                operation = null
                waitingForNewValue = true
                return
            }
            
            displayValue = formatResult(result)
            previousValue = result
        } else {
            previousValue = currentValue
        }
        
        operation = op
        waitingForNewValue = true
    }

    fun handleEquals() {
        if (errorMessage != null || operation == null) {
            return
        }
        val currentValue = displayValue.toDoubleOrNull() ?: 0.0
        val operationSymbol = when (operation) {
            '+' -> "+"
            '-' -> "-"
            '*' -> "×"
            '/' -> "÷"
            else -> ""
        }
        val result = when (operation) {
            '+' -> previousValue + currentValue
            '-' -> previousValue - currentValue
            '*' -> previousValue * currentValue
            '/' -> if (currentValue != 0.0) previousValue / currentValue else Double.NaN
            else -> currentValue
        }
        
        if (result.isNaN()) {
            displayValue = "Ошибка: деление на 0"
            errorMessage = "Ошибка: деление на 0"
            history = history + "${formatResult(previousValue)} $operationSymbol ${formatResult(currentValue)} = Ошибка"
        } else {
            val formattedResult = formatResult(result)
            displayValue = formattedResult
            history = history + "${formatResult(previousValue)} $operationSymbol ${formatResult(currentValue)} = $formattedResult"
            // Ограничиваем историю последними 10 записями
            if (history.size > 10) {
                history = history.takeLast(10)
            }
        }
        previousValue = 0.0
        operation = null
        waitingForNewValue = true
    }

    fun handleClear() {
        displayValue = "0"
        previousValue = 0.0
        operation = null
        waitingForNewValue = false
        errorMessage = null
    }

    fun handleBackspace() {
        if (errorMessage != null) {
            return
        }
        if (displayValue.length > 1 && displayValue != "0") {
            displayValue = displayValue.dropLast(1)
        } else {
            displayValue = "0"
        }
    }

    fun handleClearEntry() {
        if (errorMessage != null) {
            return
        }
        displayValue = "0"
        waitingForNewValue = false
    }

    // Функции памяти
    fun handleMemoryClear() {
        memoryValue = 0.0
        hasMemory = false
    }

    fun handleMemoryRecall() {
        if (errorMessage != null) {
            return
        }
        displayValue = formatResult(memoryValue)
        waitingForNewValue = true
    }

    fun handleMemoryStore() {
        if (errorMessage != null) {
            return
        }
        val currentValue = displayValue.toDoubleOrNull() ?: 0.0
        memoryValue = currentValue
        hasMemory = true
    }

    fun handleMemoryAdd() {
        if (errorMessage != null) {
            return
        }
        val currentValue = displayValue.toDoubleOrNull() ?: 0.0
        memoryValue += currentValue
        hasMemory = true
    }

    fun handleMemorySubtract() {
        if (errorMessage != null) {
            return
        }
        val currentValue = displayValue.toDoubleOrNull() ?: 0.0
        memoryValue -= currentValue
        hasMemory = true
    }

    // Математические функции
    fun handleReciprocal() {
        if (errorMessage != null) {
            return
        }
        val currentValue = displayValue.toDoubleOrNull() ?: 0.0
        if (currentValue == 0.0) {
            displayValue = "Ошибка: деление на 0"
            errorMessage = "Ошибка: деление на 0"
            waitingForNewValue = true
        } else {
            val result = 1.0 / currentValue
            displayValue = formatResult(result)
            waitingForNewValue = true
        }
    }

    fun handleSquareRoot() {
        if (errorMessage != null) {
            return
        }
        val currentValue = displayValue.toDoubleOrNull() ?: 0.0
        if (currentValue < 0) {
            displayValue = "Ошибка"
            errorMessage = "Ошибка: отрицательное число"
            waitingForNewValue = true
        } else {
            val result = sqrt(currentValue)
            displayValue = formatResult(result)
            waitingForNewValue = true
        }
    }

    if (isLandscape) {
        // Горизонтальная ориентация - слева история и дисплей, справа numpad
        Row(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Слева: история и дисплей
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // История вычислений - занимает всю незанятую область сверху
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 2.dp
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (history.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "История вычислений",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                reverseLayout = true
                            ) {
                                items(history.reversed()) { item ->
                                    Text(
                                        text = item,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Дисплей снизу
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.align(Alignment.TopEnd),
                            horizontalAlignment = Alignment.End
                        ) {
                            if (hasMemory) {
                                Text(
                                    text = "M",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = displayValue,
                                fontSize = if (errorMessage != null) 24.sp else 48.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End,
                                color = if (errorMessage != null) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                maxLines = 1
                            )
                        }
                    }
                }
            }
            
            // Справа: numpad
            CalculatorNumpad(
                modifier = Modifier.weight(1f),
                onNumberClick  = { handleNumberInput(it) },
                onOperationClick = { handleOperation(it) },
                onEqualsClick = { handleEquals() },
                onClearClick = { handleClear() },
                onClearEntryClick = { handleClearEntry() },
                onBackspaceClick = { handleBackspace() },
                onDecimalClick = { handleDecimalPoint() },
                onSignClick = { handleSignChange() },
                onMemoryClear = { handleMemoryClear() },
                onMemoryRecall = { handleMemoryRecall() },
                onMemoryStore = { handleMemoryStore() },
                onMemoryAdd = { handleMemoryAdd() },
                onMemorySubtract = { handleMemorySubtract() },
                onReciprocal = { handleReciprocal() },
                onSquareRoot = { handleSquareRoot() }
            )
        }
    } else {
        // Вертикальная ориентация - история сверху, дисплей и numpad снизу
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(6.dp)
        ) {
            // История вычислений - занимает всю незанятую область сверху
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .heightIn(min = 100.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 2.dp
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    reverseLayout = true
                ) {
                    items(history.reversed()) { item ->
                        Text(
                            text = item,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // Дисплей и numpad снизу
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Дисплей калькулятора
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.align(Alignment.TopEnd),
                            horizontalAlignment = Alignment.End
                        ) {
                            if (hasMemory) {
                                Text(
                                    text = "M",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = displayValue,
                                fontSize = if (errorMessage != null) 24.sp else 48.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End,
                                color = if (errorMessage != null) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Numpad
                CalculatorNumpad(
                    modifier = Modifier.fillMaxWidth(),
                    onNumberClick = { handleNumberInput(it) },
                    onOperationClick = { handleOperation(it) },
                    onEqualsClick = { handleEquals() },
                    onClearClick = { handleClear() },
                    onClearEntryClick = { handleClearEntry() },
                    onBackspaceClick = { handleBackspace() },
                    onDecimalClick = { handleDecimalPoint() },
                    onSignClick = { handleSignChange() },
                    onMemoryClear = { handleMemoryClear() },
                    onMemoryRecall = { handleMemoryRecall() },
                    onMemoryStore = { handleMemoryStore() },
                    onMemoryAdd = { handleMemoryAdd() },
                    onMemorySubtract = { handleMemorySubtract() },
                    onReciprocal = { handleReciprocal() },
                    onSquareRoot = { handleSquareRoot() }
                )
            }
        }
    }
}

@Composable
fun CalculatorNumpad(
    onNumberClick: (String) -> Unit,
    onOperationClick: (Char) -> Unit,
    onEqualsClick: () -> Unit,
    onClearClick: () -> Unit,
    onClearEntryClick: () -> Unit,
    onBackspaceClick: () -> Unit,
    onDecimalClick: () -> Unit,
    onSignClick: () -> Unit,
    onMemoryClear: () -> Unit,
    onMemoryRecall: () -> Unit,
    onMemoryStore: () -> Unit,
    onMemoryAdd: () -> Unit,
    onMemorySubtract: () -> Unit,
    onReciprocal: () -> Unit,
    onSquareRoot: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Строка 1: MC, MR, MS, M+, M- (по 1 клетке = 5 клеток)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalculatorButton(
                text = "MC",
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                textColor = MaterialTheme.colorScheme.onTertiaryContainer
            ) {
                onMemoryClear()
            }
            CalculatorButton(
                text = "MR",
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                textColor = MaterialTheme.colorScheme.onTertiaryContainer
            ) {
                onMemoryRecall()
            }
            CalculatorButton(
                text = "MS",
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                textColor = MaterialTheme.colorScheme.onTertiaryContainer
            ) {
                onMemoryStore()
            }
            CalculatorButton(
                text = "M+",
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                textColor = MaterialTheme.colorScheme.onTertiaryContainer
            ) {
                onMemoryAdd()
            }
            CalculatorButton(
                text = "M-",
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                textColor = MaterialTheme.colorScheme.onTertiaryContainer
            ) {
                onMemorySubtract()
            }
        }

        // Строка 2: <- (2 клетки), +/-, С (2 клетки) = 5 клеток
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalculatorButton(
                text = "←",
                modifier = Modifier.weight(2f),
                backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                textColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                onBackspaceClick()
            }
            CalculatorButton(
                text = "+/-",
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                textColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                onSignClick()
            }
            CalculatorButton(
                text = "C",
                modifier = Modifier.weight(2f),
                backgroundColor = MaterialTheme.colorScheme.errorContainer,
                textColor = MaterialTheme.colorScheme.onErrorContainer
            ) {
                onClearClick()
            }
        }

        // Строка 3: 1, 2, 3, корень, 1/x = 5 клеток
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalculatorButton(
                text = "1",
                modifier = Modifier.weight(1f)
            ) {
                onNumberClick("1")
            }
            CalculatorButton(
                text = "2",
                modifier = Modifier.weight(1f)
            ) {
                onNumberClick("2")
            }
            CalculatorButton(
                text = "3",
                modifier = Modifier.weight(1f)
            ) {
                onNumberClick("3")
            }
            CalculatorButton(
                text = "√",
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                textColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                onSquareRoot()
            }
            CalculatorButton(
                text = "1/x",
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                textColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                onReciprocal()
            }
        }

        // Строка 4: 4, 5, 6, +, * = 5 клеток
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalculatorButton(
                text = "4",
                modifier = Modifier.weight(1f)
            ) {
                onNumberClick("4")
            }
            CalculatorButton(
                text = "5",
                modifier = Modifier.weight(1f)
            ) {
                onNumberClick("5")
            }
            CalculatorButton(
                text = "6",
                modifier = Modifier.weight(1f)
            ) {
                onNumberClick("6")
            }
            CalculatorButton(
                text = "+",
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                textColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                onOperationClick('+')
            }
            CalculatorButton(
                text = "*",
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                textColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                onOperationClick('*')
            }
        }

        // Строка 5: 7, 8, 9, -, / = 5 клеток
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalculatorButton(
                text = "7",
                modifier = Modifier.weight(1f)
            ) {
                onNumberClick("7")
            }
            CalculatorButton(
                text = "8",
                modifier = Modifier.weight(1f)
            ) {
                onNumberClick("8")
            }
            CalculatorButton(
                text = "9",
                modifier = Modifier.weight(1f)
            ) {
                onNumberClick("9")
            }
            CalculatorButton(
                text = "-",
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                textColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                onOperationClick('-')
            }
            CalculatorButton(
                text = "/",
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                textColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                onOperationClick('/')
            }
        }

        // Строка 6: 0 (2 клетки), ., = (2 клетки) = 5 клеток
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalculatorButton(
                text = "0",
                modifier = Modifier.weight(2f)
            ) {
                onNumberClick("0")
            }
            CalculatorButton(
                text = ".",
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                textColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                onDecimalClick()
            }
            CalculatorButton(
                text = "=",
                modifier = Modifier.weight(2f),
                backgroundColor = MaterialTheme.colorScheme.primary,
                textColor = MaterialTheme.colorScheme.onPrimary
            ) {
                onEqualsClick()
            }
        }
        
    }
}

@Composable
fun CalculatorButton(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        color = backgroundColor,
        tonalElevation = 2.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalculatorPreview() {
    LT1Theme {
        CalculatorScreen()
    }
}
