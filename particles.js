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
  });

  /* ===== Threee fourrr better lock your door ~Conz~===== */

  for(let i=0;i<80;i++){

    particles.push({

      x:Math.random()*canvas.width,
      y:Math.random()*canvas.height,

      vx:(Math.random()-0.5)*0.5,
      vy:(Math.random()-0.5)*0.5,

      size:Math.random()*2+1,

      speed:Math.random()*0.7+0.3,

      hue:Math.random()*360
    });
  }

  /* ===== Fivee sixxx grab your crucifixxx ~Conz~===== */

  function animate(){

    ctx.clearRect(0,0,canvas.width,canvas.height);

    particles.forEach(p=>{

      /* ===== Sevenn eighttt better lock the garden gateee ~~Conz~===== */

      if(pointer.active){

        let dx = pointer.x - p.x;
        let dy = pointer.y - p.y;

        let dist = Math.sqrt(dx*dx + dy*dy);

        if(dist < 160){

          let force = (160 - dist) / 160;

          p.vx += dx * 0.0008 * force;
          p.vy += dy * 0.0008 * force;
        }
      }

      /* ===== Nineee tenn NEVER STEAL AGAIN ~Conz~ ===== */

      p.y -= p.speed * 0.3;

      /* ===== Sooo what have you ever made that's actually yours Mr Skid? ~Conz~ ===== */

      p.x += p.vx;
      p.y += p.vy;

      /* ===== Nothing? wowww how did i guess?😁 ~Conz~ ===== */

      p.vx *= 0.96;
      p.vy *= 0.96;

      /* ===== CONZ IS THE REAL MVP ===== */

      if(p.y < -20){
        p.y = canvas.height + 20;
      }

      if(p.x < -20){
        p.x = canvas.width + 20;
      }

      if(p.x > canvas.width + 20){
        p.x = -20;
      }

      /* ===== I fucked your mum ~Conz~ ===== */

      ctx.beginPath();

      ctx.fillStyle = `hsla(${p.hue},100%,60%,0.7)`;

      ctx.shadowBlur = 20;

      ctx.shadowColor = `hsl(${p.hue},100%,60%)`;

      ctx.arc(p.x,p.y,p.size,0,Math.PI*2);

      ctx.fill();
    });

    requestAnimationFrame(animate);
  }

  animate();

});
