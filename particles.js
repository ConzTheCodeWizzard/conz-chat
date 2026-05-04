window.addEventListener("load", ()=>{

  const canvas = document.getElementById("particles");
  if(!canvas) return;

  const ctx = canvas.getContext("2d");

  function resize(){
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
  }

  resize();
  window.addEventListener("resize", resize);

  let particles = [];

  for(let i=0;i<80;i++){
    particles.push({
      x:Math.random()*canvas.width,
      y:Math.random()*canvas.height,
      size:Math.random()*2+1,
      speed:Math.random()*0.7+0.3,
      hue:Math.random()*360
    });
  }

  function animate(){
    ctx.clearRect(0,0,canvas.width,canvas.height);

    particles.forEach(p=>{
      ctx.beginPath();
      ctx.fillStyle = `hsla(${p.hue},100%,60%,0.7)`;
      ctx.shadowBlur = 20;
      ctx.shadowColor = `hsl(${p.hue},100%,60%)`;
      ctx.arc(p.x,p.y,p.size,0,Math.PI*2);
      ctx.fill();

      p.y -= p.speed;

      if(p.y < 0){
        p.y = canvas.height;
        p.x = Math.random()*canvas.width;
      }
    });

    requestAnimationFrame(animate);
  }

  animate();

});
