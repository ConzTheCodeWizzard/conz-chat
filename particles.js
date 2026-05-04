window.onload=()=>{
  const canvas=document.getElementById("particles");
  if(!canvas) return;

  const ctx=canvas.getContext("2d");

  function resize(){
    canvas.width=window.innerWidth;
    canvas.height=window.innerHeight;
  }
  resize();
  window.onresize=resize;

  let particles=[];
  for(let i=0;i<80;i++){
    particles.push({
      x:Math.random()*canvas.width,
      y:Math.random()*canvas.height,
      s:Math.random()*2+1
    });
  }

  function draw(){
    ctx.clearRect(0,0,canvas.width,canvas.height);

    particles.forEach(p=>{
      ctx.fillStyle="#ff0044";
      ctx.beginPath();
      ctx.arc(p.x,p.y,p.s,0,Math.PI*2);
      ctx.fill();

      p.y-=0.5;
      if(p.y<0) p.y=canvas.height;
    });

    requestAnimationFrame(draw);
  }

  draw();
};
