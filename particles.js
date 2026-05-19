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

  /* ===== Oneee twooo Freddys coming for youuu ~Conz~ ===== */

  let pointer = {
    x:null,
    y:null,
    active:false
  };

  canvas.addEventListener("mousemove", e=>{

    pointer.x = e.clientX;
    pointer.y = e.clientY;
    pointer.active = true;
  });

  canvas.addEventListener("mouseleave", ()=>{

    pointer.active = false;

    particles.forEach(p=>{

      p.vx += (Math.random()-0.5) * 6;
      p.vy += (Math.random()-0.5) * 6;
    });
  });

  canvas.addEventListener("mouseup", ()=>{

    particles.forEach(p=>{

      p.vx += (Math.random()-0.5) * 6;
      p.vy += (Math.random()-0.5) * 6;
    });
  });

  canvas.addEventListener("touchstart", e=>{

    pointer.x = e.touches[0].clientX;
    pointer.y = e.touches[0].clientY;
    pointer.active = true;
  });

  canvas.addEventListener("touchmove", e=>{

    pointer.x = e.touches[0].clientX;
    pointer.y = e.touches[0].clientY;
    pointer.active = true;
  });

  canvas.addEventListener("touchend", ()=>{

    pointer.active = false;

    particles.forEach(p=>{

      p.vx += (Math.random()-0.5) * 0.8;
      p.vy += (Math.random()-0.5) * 0.8;
    });
  });

  /* ===== Threee fourrr better lock your door ~Conz~===== */

  for(let i=0;i<190;i++){

    particles.push({

      x:Math.random()*canvas.width,
      y:Math.random()*canvas.height,

      vx:(Math.random()-0.5)*1.2,
      vy:(Math.random()-0.5)*1.2,

      size:Math.random()*3.5+0.8,

      speed:Math.random()*1.2+0.5,

      hue:Math.random()*360
    });
  }

  /* ===== Fivee sixxx grab your crucifixxx ~Conz~===== */

  function animate(){

    ctx.clearRect(
0,
0,
canvas.width,
canvas.height
);

    particles.forEach(p=>{

      /* ===== Sevenn eighttt better lock the garden gateee ~~Conz~===== */

      if(pointer.active){

        let dx = pointer.x - p.x;
        let dy = pointer.y - p.y;

        let dist = Math.sqrt(dx*dx + dy*dy);

        if(dist < 260){

          let force = (260 - dist) / 260;

          p.vx += dx * 0.0035 * force;
          p.vy += dy * 0.0035 * force;
        }
      }

      /* ===== Nineee tenn NEVER STEAL AGAIN ~Conz~ ===== */

      p.y -= p.speed * 0.15;

      /* ===== Sooo what have you ever made that's actually yours Mr Skid? ~Conz~ ===== */

      p.x += p.vx;
      p.y += p.vy;

      /* ===== Nothing? wowww how did i guess?😁 ~Conz~ ===== */

      p.vx *= 0.985;
      p.vy *= 0.985;

      /* ===== CONZ IS THE REAL MVP ===== */

      if(p.x <= 0 || p.x >= canvas.width){
        p.vx *= -1;
      }

      if(p.y <= 0 || p.y >= canvas.height){
        p.vy *= -1;
      }

      /* ===== I fucked your mum ~Conz~ ===== */

      let speedGlow =
      Math.abs(p.vx) + Math.abs(p.vy);
      ctx.globalAlpha = 0.55 + (p.size / 4);
      ctx.beginPath();

      ctx.fillStyle = `hsla(${p.hue},100%,68%,0.45)`;

      ctx.shadowBlur = 18;

      ctx.shadowColor =
      `hsl(${p.hue},100%,60%)`;

      ctx.arc(p.x,p.y,p.size,0,Math.PI*2);

      ctx.fill();
    });

    requestAnimationFrame(animate);
  }

  animate();

});
