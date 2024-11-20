# FDS-FX | JavaFX Fluid Dynamic Simulator
### JavaFX Fluid Dynamics Simulator is a simple computational fluid dynamics simulator inspired by [***Ten Minute Physics***](https://github.com/matthias-research/pages/blob/master/tenMinutePhysics/17-fluidSim.html) and is a tweaked version of the fluidSim project with added features.
![alt text](https://github.com/AryA-65/FDS-FX/blob/afaa744dc7fb5695844878ea14d0cd0bd2ffa989/FDSLG.png "FDS Logo")

## JDK Installation
**To be added in the near future**

## To Do
Many, many things. Add:
* SVG/PNG importation to simulation ![25%](https://progress-bar.xyz/25)
* Seperate simulation time from drawing time (HOW????)
* Main menu and simulation menu for better user experience
* 3D rendering with 3D object simulation (biggest side project) ![25%](https://progress-bar.xyz/25)
* Heat transfer (for electronics, vehicles, etc)
* Higher Resolution Simulation (won't be real time, will be heavily GPU entensive, should be playback possible) ![75%](https://progress-bar.xyz/75)
* Save replay data (JSON, TXT, Custom File Format?)
* Suggestions:
  * ------

## Version Control
|Version|Date|Decription|Features|Bugs|
|-------|----|----------|--------|----|
|V0.2|19/11/24|Replays and Advanced 3D|-Added replays, recording higher resolution on playing back the recording in real time.<br>-Added more advanced camera operation for 3D simulation<br>-Partial implementation of image simulation|-Recording/replay is based on time rather than frames.<br>-Optimisation required.<br>-Adding image to simulation only shows the image shadow on canvas + image is stretched to fit canvas (should preserve size rather than fit to size) + only PNG|
|V0.15.5|17/11/24|3D Object Importation|Added 3D Object Importation with very limited functionality. Camera rotation around axes will be available soon!|-Not a bug related to the program, but camera or object might be unaligned with the chosen axis (could be an object only issue, as public 3D models were used for testing)|
|V0.15|14/11/24|Precision Simulation|Added iteration and resolution sliders that can help get more accurate simulations|-**Major:** running project on less cores or cores with lower clocks and toggling vectors drops fps from 60fps avg (monitor capped) to around 20-25fps<br> -Not really a bug, but could use optimisation|
|V0.1|11/11/24|Basic Simulation with Circle|Simulation with circle. Able to view: pressure, streamlines, smoke, fps and vectors.|- Simulation doesn't work properly outside or near the perimeter of canvas.<br>- Circle reinitialisation doesn't change fluid sim until dragged.|
|Initial Commit|26/10/2024|Initial Commit|NaN|Nan|

## Sources
1. [***Ten Minute Physics Fluid Simulation Github***](https://github.com/matthias-research/pages/blob/master/tenMinutePhysics/17-fluidSim.html)
2. [***Ten Minute Physics Youtube***](https://www.youtube.com/c/TenMinutePhysics)
