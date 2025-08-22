package com.example.noditos

import android.R
import android.graphics.drawable.Icon
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.xr.scenecore.Component
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppNavigation()
            //CurvedArrowExample()
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
fun Pizarra(nombre: String) {
    data class Nodo(
        val id: Int,
        val title: String,
        var position: Offset
    )

    val nodos = remember { mutableStateListOf<Nodo>() }
    var nextId = 0

    fun addNodo(position: Offset){
        nodos.add(Nodo(id = ++nextId,title = nextId.toString(), position = position))
    }

    val density = LocalDensity.current
    var selectedNodeIndex by remember { mutableIntStateOf(-1) }
    var isDragging by remember { mutableStateOf(false) }
    var isOverTrash by remember { mutableStateOf(false) }
    val connections = remember { mutableStateListOf<Pair<Int, Int>>() }
    Scaffold(
        topBar = {
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.LightGray)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val minDistance = 30f
                        val isOverlapping = nodos.any { nodo ->
                            val dx = nodo.position.x - offset.x
                            val dy = nodo.position.y - offset.y
                            val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                            distance < minDistance
                        }
                        if (!isOverlapping) {
                            addNodo(offset)
                            Log.d(
                                "Click en la pizarra",
                                "Nuevo nodo anadido en ${offset.x} ,${offset.x}"
                            )
                        }
                    }
                }
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    connections.forEach { (fromIndex, toIndex) ->
                        val from = nodos[fromIndex].position
                        val to = nodos[toIndex].position
                        val radius = (24/2).dp.toPx()

                        //direccion normalizada
                        val dx = to.x - from.x
                        val dy = to.y - from.y
                        val dist = sqrt(dx*dx + dy*dy)
                        val dirX = dx / dist
                        val dirY = dy / dist


                        //Bordes de los nodos
                        val startX = from.x + dirX * radius
                        val startY = from.y + dirY * radius
                        val endX = to.x - dirX * radius
                        val endY = to.y - dirY * radius

                        //Punto de control
                        val midX = (startX + endX) / 2f
                        val midY = (startY + endY) / 2f
                        val offset = if(fromIndex>toIndex) 150f else -150f
                        val controlX = midX + offset
                        val controlY = midY

                        //dibujo de curva
                        val path = Path().apply {
                            moveTo(startX, startY)
                            quadraticBezierTo(
                                controlX,
                                controlY,
                                endX,
                                endY
                            )
                        }
                        drawPath(
                            path = path,
                            color = Color.Black,
                            style = Stroke(width = 5f)
                        )
                        val dxArrow = endX - controlX
                        val dyArrow = endY - controlY
                        //dibujando la punta de la flecha
                        val angle = atan2(dyArrow,dxArrow)
                        val arrowSize = 40f
                        val arrowAngle = Math.PI / 6
                        val x1 = endX - arrowSize * cos(angle - arrowAngle).toFloat()
                        val y1 = endY - arrowSize * sin(angle - arrowAngle).toFloat()
                        val x2 = endX - arrowSize * cos(angle + arrowAngle).toFloat()
                        val y2 = endY - arrowSize * sin(angle + arrowAngle).toFloat()

                        drawLine(Color.Black, Offset(endX, endY), Offset(x1, y1), strokeWidth = 6f)
                        drawLine(Color.Black, Offset(endX, endY), Offset(x2, y2), strokeWidth = 6f)

                    }
                }
            }

            //Dibujar los noditos almacenados
            nodos.forEach { nodo ->

                Log.d(
                    "Dibujando en la pizarra",
                    "Nuevo nodo añadido en ${nodo.position.x} ,${nodo.position.y}"
                )

                val circleColor: Color
                val circleSize: Int
                val circleOffset: Int
                if (nodo.id != selectedNodeIndex) {
                    circleColor = Color(153, 217, 234)
                    circleSize = 24
                    circleOffset = -12
                } else {
                    circleColor = Color(82, 192, 220)
                    circleSize = 32
                    circleOffset = -16
                }
                val screenHeight =
                    with(density) { LocalConfiguration.current.screenHeightDp.dp.toPx() }
                val redZoneHeight = 80.dp
                val redZoneHeightPx = with(density) { redZoneHeight.toPx() }

                //Componente que se ve como el círculo del nodo
                Box(
                    modifier = Modifier
                        .offset(
                            with(density) { nodo.position.x.toDp() },
                            with(density) { nodo.position.y.toDp() })
                        .offset(circleOffset.dp, circleOffset.dp)
                        .size(circleSize.dp)
                        .background(circleColor, CircleShape)
                        .clickable {
                            if (selectedNodeIndex == -1) {
                                selectedNodeIndex = nodo.id
                            } else {
                                if (!connections.contains(Pair(selectedNodeIndex, nodo.id))) {
                                    connections.add(Pair(selectedNodeIndex, nodo.id))
                                }
                                selectedNodeIndex = -1
                            }
                        }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { isDragging = true },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val newX = nodo.position.x + dragAmount.x
                                    val newY = nodo.position.y + dragAmount.y
                                    nodo.position = Offset(newX, newY)
                                    //verificacion de la posicion del nodo
                                    isOverTrash = newY + 60f > (screenHeight - redZoneHeightPx)
                                },
                                onDragEnd = {
                                    if (isOverTrash){
                                        nodos.removeAt(nodo.id)
                                        connections.removeAll{ (from, to) ->
                                            (from == nodo.id || to == nodo.id)
                                        }
                                    }
                                    isDragging = false
                                    isOverTrash = false
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = nodo.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (isDragging) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(if (isOverTrash) 95.dp else 80.dp)
                        .background(
                            Color(
                                red = 1f,
                                green = 0f,
                                blue = 0f,
                                alpha = if (isOverTrash) 0.9f else 0.6f
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = Color.White,
                        modifier = Modifier.size(if (isOverTrash) 45.dp else 35.dp)
                    )
                }
            }
        }

    }
}

