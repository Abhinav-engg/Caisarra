package com.abhinav.caisarra.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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

@Composable
fun CountryPicker(
    selectedCode: String,
    onCountrySelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = Color(0xFF151F2E),
    textColor: Color = Color(0xFFF3F6FC),
    borderColor: Color = Color(0xFF29384A),
    accentColor: Color = Color(0xFF08D5E5)
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    val selectedCountry = CountryUtils.find(selectedCode)

    Column(modifier = modifier.fillMaxWidth()) {

        Text(
            text = "Country",
            color = textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(backgroundColor)
                    .border(
                        width = 1.dp,
                        color = if (expanded) accentColor else borderColor,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable(enabled = enabled) {
                        expanded = true
                    }
                    .padding(
                        horizontal = 14.dp,
                        vertical = 15.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = selectedCountry?.flag ?: "🌐",
                    fontSize = 22.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = selectedCountry?.name ?: "Select country",
                        color = textColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = selectedCountry?.code
                            ?: "Choose your country",
                        color = textColor.copy(alpha = 0.65f),
                        fontSize = 11.sp
                    )
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Select country",
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                },
                modifier = Modifier
                    .background(backgroundColor)
            ) {

                DropdownMenuItem(
                    text = {
                        Text(
                            text = "🌐  Not set",
                            color = textColor
                        )
                    },
                    onClick = {
                        onCountrySelected("")
                        expanded = false
                    }
                )

                CountryUtils.all().forEach { country ->

                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Text(
                                    text = country.flag,
                                    fontSize = 20.sp
                                )

                                Text(
                                    text = "  ${country.name} (${country.code})",
                                    color = textColor,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        },
                        onClick = {
                            onCountrySelected(country.code)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}