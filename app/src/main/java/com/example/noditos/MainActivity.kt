package com.example.noditos

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.xr.scenecore.Component
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppNavigation()
        }
    }
}

sealed class Screen(val route: String){
    object Menu: Screen("menu")
    object Pizarra: Screen("pizarra")
    object Grafos: Screen("grafos")
    object Otros: Screen("otros")
}
@Composable
fun AppNavigation(){
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.Menu.route
    ){
        composable(Screen.Menu.route) { MenuScreen(navController) }
        composable(Screen.Pizarra.route) { Pizarra("Pizarra") }
        composable(Screen.Grafos.route) { GenericScreen("Grafos Guardados") }
        composable(Screen.Otros.route) { GenericScreen("Otros") }
    }
}
// --------------Mis pantallas-------------

@Composable
fun MenuScreen(navController: NavHostController){
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { navController.navigate(Screen.Pizarra.route) }
            ) { Text("Pizarra") }
            Button(
                onClick = { navController.navigate(Screen.Grafos.route) }
            ) { Text("Grafos guardados") }
            Button(
                onClick = { navController.navigate(Screen.Otros.route) }
            ) { Text("Otros") }
        }
    }
}
@Composable
fun GenericScreen(nombre: String){
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Text("Pantalla de ${nombre}", style = MaterialTheme.typography.headlineMedium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Pizarra(nombre: String){
    val nodosPositions = remember{ mutableStateListOf<Pair<Float, Float>>() }
    val density = LocalDensity.current
    var selectedNodeIndex by remember { mutableIntStateOf(-1) }
    var modo: String by remember { mutableStateOf("agregar") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pizarra") },
                actions = {
                    IconButton(onClick = { modo = "agregar" }) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar nodo")
                    }
                    IconButton(onClick = { modo = "relacionar" }) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Relacionar nodo")
                    }
                    IconButton(onClick = { modo = "eliminar" }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar nodo")
                    }
                }
            )
        }
    ) {
        innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.LightGray)
                .pointerInput(Unit){
                    detectTapGestures{  offset ->
                        when(modo){
                            "agregar" -> {
                                val minDistance = 30f
                                val isOverlapping = nodosPositions.any {
                                        pos ->
                                    val dx = pos.first - offset.x
                                    val dy = pos.second - offset.y
                                    val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                                    distance < minDistance
                                }
                                if(!isOverlapping){
                                    nodosPositions.add(Pair(offset.x, offset.y))
                                    Log.d("Click en la pizarra", "Nuevo nodo anadido en ${offset.x} ,${offset.x}")
                                }
                            }
                        }
                    }
                }
        ) {
            //Dibujar los noditos almacenados
            nodosPositions.forEachIndexed{ index, position ->
                val x = position.first
                val y = position.second

                Log.d("Dibujando en la pizarra", "Nuevo nodo añadido en ${x} ,${y}")

                val xdp = with(density) { x.toDp() }
                val ydp = with(density) { y.toDp() }
                val circleColor: Color
                val circleSize: Int
                if(index!=selectedNodeIndex){
                    circleColor = Color(153,217,234)
                    circleSize = 24
                } else{
                    circleColor = Color(82,192,220)
                    circleSize = 32
                }

                Box(
                    modifier = Modifier
                        .offset(xdp, ydp)
                        .offset((-12).dp, (-12).dp)
                        .size(circleSize.dp)
                        .background(circleColor, CircleShape)
                        .clickable{
                            selectedNodeIndex = if (selectedNodeIndex != index)  index else -1
                        },
                    contentAlignment = Alignment.Center
                ){
                    Text(
                        text = (index+1).toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

    }

}