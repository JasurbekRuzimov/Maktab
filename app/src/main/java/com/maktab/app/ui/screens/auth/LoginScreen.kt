package com.maktab.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maktab.app.ui.theme.*
import com.maktab.app.ui.theme.str
import kotlinx.coroutines.delay
import com.maktab.app.R
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onSuccess: (session: com.maktab.app.network.SessionInfo) -> Unit
) {
    var username       by remember { mutableStateOf("") }
    var password       by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading      by remember { mutableStateOf(false) }
    var errorMsg       by remember { mutableStateOf("") }

    val scope        = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    var contentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { delay(80); contentVisible = true }

    fun doLogin() {
        if (username.isBlank() || password.isBlank()) {
            errorMsg = "Foydalanuvchi nomi va parolni kiriting"
            return
        }
        errorMsg = ""
        scope.launch {
            isLoading = true
            val authRepo = com.maktab.app.network.AuthRepository()
            when (val result = authRepo.login(username, password)) {
                is com.maktab.app.network.ApiResult.Success -> {
                    isLoading = false
                    onSuccess(result.data)
                }
                is com.maktab.app.network.ApiResult.Error -> {
                    isLoading = false
                    errorMsg = result.message
                }
                else -> { isLoading = false }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))

            AnimatedVisibility(visible = contentVisible, enter = fadeIn() + slideInVertically { it / 3 }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    // Logo
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(TealContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.School, contentDescription = null, tint = Teal10, modifier = Modifier.size(36.dp))
                    }

                    Spacer(Modifier.height(20.dp))

                    Text(stringResource(R.string.login_title), fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(stringResource(R.string.login_subtitle), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(Modifier.height(40.dp))

                    // Username
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.login_username), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it; errorMsg = "" },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.login_username_hint), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = if (username.isNotEmpty()) Teal10 else MaterialTheme.colorScheme.onSurfaceVariant) },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Teal10,
                                focusedLeadingIconColor = Teal10,
                                cursorColor = Teal10
                            )
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Parol
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.login_password), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; errorMsg = "" },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.login_password_hint), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = if (password.isNotEmpty()) Teal10 else MaterialTheme.colorScheme.onSurfaceVariant) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); doLogin() }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Teal10,
                                focusedLeadingIconColor = Teal10,
                                cursorColor = Teal10
                            )
                        )
                    }

                    // Xato xabar
                    AnimatedVisibility(visible = errorMsg.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Red10, modifier = Modifier.size(14.dp))
                            Text(errorMsg, fontSize = 12.sp, color = Red10)
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // Kirish tugmasi
                    Button(
                        onClick = { doLogin() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Teal10),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(stringResource(R.string.login_loading), fontSize = 15.sp)
                        } else {
                            Text(stringResource(R.string.login_button), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}