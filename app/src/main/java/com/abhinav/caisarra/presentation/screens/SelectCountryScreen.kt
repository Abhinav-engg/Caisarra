package com.abhinav.caisarra.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.presentation.components.CountryOption
import com.abhinav.caisarra.presentation.components.CountryUtils
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
private val CountryBackground = Color(0xFF080D13)
private val CountryCard = Color(0xFF151923)
private val CountryRow = Color(0xFF101923)
private val CountryText = Color(0xFFE7EAF0)
private val CountryMuted = Color(0xFF8793A5)
private val CountryAccent = Color(0xFF00D8E8)
private val CountryDivider = Color(0xFF29313D)

@Composable
fun SelectCountryScreen(
    selectedCountryCode: String?,
    onBack: () -> Unit,
    onCountrySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var search by remember { mutableStateOf("") }

    val countries = remember(search) {
        CountryUtils.all().filter { country ->
            search.isBlank() ||
                    country.name.contains(search.trim(), ignoreCase = true) ||
                    country.code.contains(search.trim(), ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CountryBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
    ) {


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = CountryText
                )
            }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SELECT COUNTRY",
                    color = CountryText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp
                )
            }

            Spacer(Modifier.size(44.dp))
        }

        Spacer(Modifier.height(12.dp))


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 4.dp
                    )
                )
                .background(CountryCard)
                .padding(horizontal = 14.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = CountryText,
                    fontSize = 14.sp
                ),
                decorationBox = { innerTextField ->
                    Box {
                        if (search.isEmpty()) {
                            Text(
                                text = "Search...",
                                color = CountryMuted.copy(alpha = 0.7f),
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )

            if (search.isNotEmpty()) {
                IconButton(
                    onClick = { search = "" },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear search",
                        tint = CountryMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search countries",
                    tint = CountryMuted,
                    modifier = Modifier.size(19.dp)
                )
            }
        }


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(CountryRow)
                .clickable { onCountrySelected("") }
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🌐", fontSize = 20.sp)

            Spacer(Modifier.width(12.dp))

            Text(
                text = "Not set",
                modifier = Modifier.weight(1f),
                color = CountryText,
                fontSize = 13.sp
            )

            if (selectedCountryCode.isNullOrBlank()) {
                Text(
                    text = "✓",
                    color = CountryAccent,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(CountryRow)
        ) {
            items(
                items = countries,
                key = { it.code }
            ) { country ->
                CountryListItem(
                    country = country,
                    selected = country.code.equals(
                        selectedCountryCode,
                        ignoreCase = true
                    ),
                    onClick = {
                        onCountrySelected(country.code)
                    }
                )
            }

            if (countries.isEmpty()) {
                item {
                    Text(
                        text = "No countries found",
                        color = CountryMuted,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CountryListItem(
    country: CountryOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (selected) CountryAccent.copy(alpha = 0.08f)
                    else CountryRow
                )
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = country.name,
                modifier = Modifier.weight(1f),
                color = CountryText,
                fontSize = 13.sp
            )

            Text(
                text = country.code,
                color = CountryMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.width(12.dp))

            Text(
                text = country.flag,
                fontSize = 21.sp
            )

            if (selected) {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "✓",
                    color = CountryAccent,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(CountryDivider)
        )
    }
}