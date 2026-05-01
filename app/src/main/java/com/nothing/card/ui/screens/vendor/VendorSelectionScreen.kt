package com.nothing.card.ui.screens.vendor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.nothing.card.data.model.Vendor
import com.nothing.card.ui.components.DotMatrixText
import com.nothing.card.ui.theme.NothingBlack
import com.nothing.card.ui.theme.NothingWhite
import com.nothing.card.ui.components.NothingSnackbar
import com.nothing.card.ui.theme.SpaceMonoFamily
import com.nothing.card.ui.screens.add.AddCardViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorSelectionScreen(
    onVendorSelected: (Vendor, String?, String?) -> Unit, // (vendor, barcode, format)
    onBack: () -> Unit,
    viewModel: AddCardViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val suggestions by viewModel.suggestions.collectAsState()
    var selectedVendor by remember { mutableStateOf<Vendor?>(null) }
    val sheetState = rememberModalBottomSheetState()
    var showOptions by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            showOptions = false
            val scanner = BarcodeScanning.getClient()
            try {
                val image = InputImage.fromFilePath(context, uri)
                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        if (barcodes.isNotEmpty()) {
                            val barcode = barcodes[0]
                            val formatString = getBarcodeFormatString(barcode.format)
                            onVendorSelected(selectedVendor!!, barcode.rawValue, formatString)
                        } else {
                            // No barcode found, inform user and navigate
                            scope.launch {
                                snackbarHostState.showSnackbar("No barcode detected. Please enter manually.")
                            }
                            onVendorSelected(selectedVendor!!, null, null)
                        }
                    }
                    .addOnFailureListener {
                        onVendorSelected(selectedVendor!!, null, null)
                    }
            } catch (e: Exception) {
                onVendorSelected(selectedVendor!!, null, null)
            }
        }
    }

    Scaffold(
        containerColor = NothingBlack,
        snackbarHost = { 
            SnackbarHost(snackbarHostState) { data ->
                NothingSnackbar(data)
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("Add Card", style = MaterialTheme.typography.headlineMedium, color = NothingWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NothingWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NothingBlack)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            SearchBar(
                query = searchQuery,
                vendorCount = viewModel.getVendorCount(),
                onQueryChange = { 
                    searchQuery = it
                    viewModel.onNameChanged(it)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                item {
                    Text(
                        text = "POPULAR STORES",
                        style = MaterialTheme.typography.labelSmall,
                        color = NothingWhite.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(suggestions) { vendor ->
                    VendorItem(
                        vendor = vendor,
                        onClick = {
                            selectedVendor = vendor
                            showOptions = true
                        }
                    )
                }
            }
        }

        if (showOptions && selectedVendor != null) {
            ModalBottomSheet(
                onDismissRequest = { showOptions = false },
                sheetState = sheetState,
                containerColor = NothingBlack,
                dragHandle = { BottomSheetDefaults.DragHandle(color = NothingWhite.copy(alpha = 0.3f)) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 48.dp, start = 24.dp, end = 24.dp, top = 8.dp)
                ) {
                    Text(
                        text = selectedVendor!!.name.uppercase(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = NothingWhite,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    
                    OptionItem(
                        title = "SCAN WITH CAMERA",
                        icon = Icons.Default.CameraAlt,
                        onClick = {
                            showOptions = false
                            onVendorSelected(selectedVendor!!, "", "")
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OptionItem(
                        title = "UPLOAD FROM GALLERY",
                        icon = Icons.Default.PhotoLibrary,
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )
                }
            }
        }
    }
}

private fun getBarcodeFormatString(format: Int): String {
    return when (format) {
        Barcode.FORMAT_CODE_128 -> "CODE_128"
        Barcode.FORMAT_CODE_39 -> "CODE_39"
        Barcode.FORMAT_CODE_93 -> "CODE_93"
        Barcode.FORMAT_EAN_13 -> "EAN_13"
        Barcode.FORMAT_EAN_8 -> "EAN_8"
        Barcode.FORMAT_QR_CODE -> "QR_CODE"
        Barcode.FORMAT_UPC_A -> "UPC_A"
        Barcode.FORMAT_UPC_E -> "UPC_E"
        Barcode.FORMAT_PDF417 -> "PDF417"
        Barcode.FORMAT_AZTEC -> "AZTEC"
        Barcode.FORMAT_ITF -> "ITF"
        else -> "QR_CODE"
    }
}

@Composable
fun VendorItem(vendor: Vendor, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = NothingWhite.copy(alpha = 0.05f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(android.graphics.Color.parseColor(vendor.colorHex)))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = vendor.name, color = NothingWhite, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun OptionItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = NothingWhite.copy(alpha = 0.05f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = NothingWhite)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, color = NothingWhite, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String, 
    vendorCount: Int,
    onQueryChange: (String) -> Unit, 
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { 
            Text(
                text = "SEARCH FROM $vendorCount STORES...", 
                color = NothingWhite.copy(alpha = 0.3f),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = SpaceMonoFamily
            ) 
        },
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(NothingWhite.copy(alpha = 0.05f)),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = NothingWhite,
            focusedTextColor = NothingWhite,
            unfocusedTextColor = NothingWhite
        ),
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NothingWhite.copy(alpha = 0.3f)) },
        singleLine = true
    )
}
