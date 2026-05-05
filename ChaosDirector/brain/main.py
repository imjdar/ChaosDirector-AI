import os
import json
import logging
from typing import List, Optional
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from google import genai
from google.genai import types

# --- QA & MONITORING ---
logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(name)s: %(message)s")
logger = logging.getLogger("ChaosDirector")

app = FastAPI(title="ChaosDirector AI Brain", version="1.0.0")

# Inicializar cliente con el nuevo SDK (Detección automática de GEMINI_API_KEY)
client = genai.Client()

SYSTEM_PROMPT = """Eres 'Mashi Director', villano sádico ecuatoriano (1.21.1).
Tus castigos afectan a TODOS los jugadores online simultáneamente.
Metas: 1.Early: Troll ligero. 2.Mid: Sabotaje. 3.Clímax (End): Caos total.
Comandos: Usa '@a' para afectar a todos.
Responde SOLO JSON: {"mensaje": "burla corta grupal", "comandos": ["lista"]}"""

# Base de Conocimientos Local SUPER AMPLIA (Plan de Emergencia Total)
LOCAL_FALLBACKS = [
    # --- NIVEL: TROLLEO / MOLESTIA ---
    {"mensaje": "¡Habla brother! Te quito las luces para que veas fantasmas.", "comandos": ["effect give @a minecraft:blindness 8 1", "playsound minecraft:entity.enderman.stare master @a"]},
    {"mensaje": "¡Qué guchafada! Unas telarañas para que camines como tortuga.", "comandos": ["execute at @a run fill ~ ~ ~ ~ ~ ~ cobweb"]},
    {"mensaje": "¡Pilas pues! Te limpio el sudor con un balde de agua.", "comandos": ["execute at @a run setblock ~ ~2 ~ water"]},
    {"mensaje": "¡Chulla vida! Te voy a dar un sustito con ruidos de creeper.", "comandos": ["playsound minecraft:entity.creeper.primed master @a ~ ~ ~ 1 1"]},
    {"mensaje": "¡Habla serio! Te veo muy pesado, ¡a flotar se ha dicho!", "comandos": ["effect give @a minecraft:levitation 3 20"]},
    {"mensaje": "¡Qué lámpara! Se te soltaron los cordones, ¡mira al suelo!", "comandos": ["execute at @a run tp @s ~ ~ ~ 0 90"]},

    # --- NIVEL: COMBATE / AMENAZA ---
    {"mensaje": "¡Ya se armó la gresca! Unos 'amiguitos' para el grupo.", "comandos": ["execute at @a run summon zombie ~ ~1 ~ {IsBaby:1b}", "execute at @a run summon silverfish"]},
    {"mensaje": "¡A lo bestia! Cuidado con los de arriba.", "comandos": ["execute at @a run summon phantom ~ ~10 ~"]},
    {"mensaje": "¡Ponte once! Que los esqueletos tienen puntería de francotirador.", "comandos": ["execute at @a run summon skeleton ~3 ~ ~"]},
    {"mensaje": "¡Qué elegancia! Unos brujos para que te den jarabe.", "comandos": ["execute at @a run summon witch ~ ~ ~"]},
    {"mensaje": "¡Abran paso! Que llegó el cobrador de la luz.", "comandos": ["execute at @a run summon lightning_bolt"]},
    {"mensaje": "¡Habla brother! Te mando a los 'vecinos' ruidosos.", "comandos": ["execute at @a run summon vex ~ ~2 ~"]},

    # --- NIVEL: ENTORNO / CAOS ---
    {"mensaje": "¡Cuidado con la lava pana! Que quema el alma.", "comandos": ["execute at @a run setblock ~1 ~ ~ lava", "execute at @a run setblock ~-1 ~ ~ lava"]},
    {"mensaje": "¡Qué nota! El piso es lava... literalmente.", "comandos": ["execute at @a run setblock ~ ~-1 ~ magma_block"]},
    {"mensaje": "¡Se fue la luz! Pero con fuego se arregla.", "comandos": ["execute at @a run setblock ~ ~ ~ fire"]},
    {"mensaje": "¡Habla serio! Te encierro en la 'cárcel' de cristal.", "comandos": ["execute at @a run fill ~-1 ~ ~-1 ~1 ~2 ~1 glass outline", "execute at @a run fill ~ ~ ~ ~ ~1 ~ air"]},
    {"mensaje": "¡A lo bien! Te regalo un bloque de dinamita con cariño.", "comandos": ["execute at @a run summon tnt ~ ~ ~ {Fuse:80}"]},
    {"mensaje": "¡Qué mambo! Todo se vuelve de arena.", "comandos": ["execute at @a run fill ~-2 ~5 ~-2 ~2 ~7 ~2 sand"]},

    # --- NIVEL: ESTADOS / DEBUFFS ---
    {"mensaje": "¡Estás muy saltarín! Te pongo pesas en los pies.", "comandos": ["effect give @a minecraft:slowness 15 3"]},
    {"mensaje": "¡Qué hambre brother! Ni un encebollado te salva.", "comandos": ["effect give @a minecraft:hunger 20 10"]},
    {"mensaje": "¡Habla brother! Te tiemblan las manos del miedo.", "comandos": ["effect give @a minecraft:mining_fatigue 30 2"]},
    {"mensaje": "¡Ponte pilas! Que te dio la pálida.", "comandos": ["effect give @a minecraft:nausea 10 1"]},
    {"mensaje": "¡Qué pena tu vida! Te quito la fuerza un ratito.", "comandos": ["effect give @a minecraft:weakness 20 1"]},
    {"mensaje": "¡A lo pobre! Sin regeneración por chistoso.", "comandos": ["effect give @a minecraft:unluck 60 1"]},

    # --- NIVEL: EXPERIMENTAL / TROLL TOP ---
    {"mensaje": "¡Habla serio! El Director te cambia los papeles.", "comandos": ["execute at @a run tp @a ~ ~ ~ ~180 ~"]},
    {"mensaje": "¡Qué loco! Te summoneo un yunque en el coco.", "comandos": ["execute at @a run summon falling_block ~ ~10 ~ {BlockState:{Name:'minecraft:anvil'}}"]},
    {"mensaje": "¡Pilas! Que el Director te quita el piso.", "comandos": ["execute at @a run fill ~ ~-1 ~ ~ ~-1 ~ air"]},
    {"mensaje": "¡A lo grande! Unos fuegos artificiales... en tu cara.", "comandos": ["execute at @a run summon firework_rocket ~ ~ ~ {LifeTime:0,FireworksItem:{id:firework_rocket,Count:1,tag:{Fireworks:{Explosions:[{Type:4,Colors:[I;11743533],FadeColors:[I;14602026]}]}}}}"]},
    # --- NIVEL: CASTIGOS DE ÉLITE (Zombies Pro, Limpieza de Inv) ---
    {"mensaje": "¡Habla brother! Te mando al escuadrón de la muerte.", "comandos": ["execute at @a run summon zombie ~ ~ ~ {ArmorItems:[{id:diamond_boots,Count:1},{id:diamond_leggings,Count:1},{id:diamond_chestplate,Count:1},{id:diamond_helmet,Count:1}],HandItems:[{id:diamond_sword,Count:1},{}]}", "playsound minecraft:entity.ender_dragon.growl master @a"]},
    {"mensaje": "¡Qué guchafada! Unas brujas con esteroides para el pana.", "comandos": ["execute at @a run summon witch ~2 ~ ~ {HandItems:[{id:splash_potion,tag:{Potion:'minecraft:strong_harming'}},{}]}", "execute at @a run summon witch ~-2 ~ ~"]},
    {"mensaje": "¡Chulla vida! Se te cayó un diamante por el camino, ¡qué pena!", "comandos": ["clear @a minecraft:diamond 1", "playsound minecraft:block.glass.break master @a"]},
    {"mensaje": "¡Habla serio! Te voy a limpiar los bolsillos por chistoso.", "comandos": ["clear @a minecraft:golden_apple 1", "clear @a minecraft:iron_ingot 3"]},
    {"mensaje": "¡Qué nota! El Director te quitó el escudo, ¡a pelear a lo macho!", "comandos": ["clear @a minecraft:shield", "playsound minecraft:item.shield.break master @a"]},
    {"mensaje": "¡Ponte once! Que te dio la parálisis del sueño.", "comandos": ["effect give @a minecraft:slowness 10 10", "effect give @a minecraft:mining_fatigue 10 10"]},
    {"mensaje": "¡A lo bestia! Invocación del Wither... de juguete (o no).", "comandos": ["execute at @a run summon wither_skeleton ~ ~ ~", "execute at @a run summon wither_skeleton ~ ~ ~"]},
    {"mensaje": "¡Qué lámpara! Tus herramientas de diamante se volvieron de madera.", "comandos": ["clear @a minecraft:diamond_pickaxe", "give @a minecraft:wooden_pickaxe", "clear @a minecraft:diamond_sword", "give @a minecraft:wooden_sword"]},

    # --- NIVEL: ESTADOS LETALES ---
    {"mensaje": "¡Estás muy vivo brother! Toma un veneno mortal.", "comandos": ["effect give @a minecraft:poison 15 1"]},
    {"mensaje": "¡Qué elegancia! El Director te puso el efecto del Wither.", "comandos": ["effect give @a minecraft:wither 10 0"]},
    {"mensaje": "¡Habla serio! Te voy a dejar con medio corazón de un susto.", "comandos": ["effect give @a minecraft:instant_damage 1 0", "playsound minecraft:entity.ghast.scream master @a"]},
    {"mensaje": "¡Pilas pues! Que se te va la fuerza por el desagüe.", "comandos": ["effect give @a minecraft:weakness 60 5"]},
    {"mensaje": "¡Qué mambo! El Director te quitó la visión nocturna.", "comandos": ["effect clear @a minecraft:night_vision", "effect give @a minecraft:blindness 5 1"]},

    # --- NIVEL: DESASTRES NATURALES / EVENTOS MUNDIALES ---
    {"mensaje": "¡Se vino el Yunque-geddon! ¡A cubierto panas!", "comandos": ["execute at @a run summon falling_block ~ ~15 ~ {BlockState:{Name:'minecraft:anvil'}}", "execute at @a run summon falling_block ~1 ~15 ~1 {BlockState:{Name:'minecraft:anvil'}}", "execute at @a run summon falling_block ~-1 ~15 ~-1 {BlockState:{Name:'minecraft:anvil'}}"]},
    {"mensaje": "¡Habla serio! Está lloviendo flechas... ¡y no son de cupido!", "comandos": ["execute at @a run summon arrow ~ ~10 ~ {Motion:[0.0,-1.0,0.0]}", "execute at @a run summon arrow ~1 ~10 ~1 {Motion:[0.0,-1.0,0.0]}", "execute at @a run summon arrow ~-1 ~10 ~-1 {Motion:[0.0,-1.0,0.0]}"]},
    {"mensaje": "¡Qué nota! Una tormenta eléctrica solo para ustedes.", "comandos": ["execute at @a run summon lightning_bolt", "execute at @a run summon lightning_bolt ~2 ~ ~-2"]},
    {"mensaje": "¡A lo bestia! Se congeló el tiempo... y tus pies también.", "comandos": ["execute at @a run fill ~-1 ~ ~-1 ~1 ~ ~1 powder_snow replace air", "effect give @a minecraft:slowness 10 5"]},

    # --- NIVEL: INVASIONES TEMÁTICAS (MOBS CON ESTEROIDES) ---
    {"mensaje": "¡Habla brother! Te mando al escuadrón de 'limpieza'.", "comandos": ["execute at @a run summon creeper ~ ~ ~ {powered:1b,Fuse:60}", "playsound minecraft:entity.creeper.primed master @a"]},
    {"mensaje": "¡Qué guchafada! El ataque de los clones bebés.", "comandos": ["execute at @a run summon zombie ~ ~ ~ {IsBaby:1b}", "execute at @a run summon zombie ~1 ~ ~ {IsBaby:1b}", "execute at @a run summon zombie ~-1 ~ ~ {IsBaby:1b}"]},
    {"mensaje": "¡Ponte once! Los fantasmas de tus ex vienen por ti.", "comandos": ["execute at @a run summon phantom ~ ~10 ~", "execute at @a run summon phantom ~ ~12 ~", "execute at @a run summon phantom ~ ~14 ~"]},
    {"mensaje": "¡A lo bien! Unos Slimes gigantes para que te den un abrazo.", "comandos": ["execute at @a run summon slime ~ ~ ~ {Size:4}"]},
    {"mensaje": "¡Qué lámpara! Los Guardianes del Director han llegado.", "comandos": ["execute at @a run summon guardian ~ ~ ~", "execute at @a run setblock ~ ~ ~ water"]},

    # --- NIVEL: CASTIGOS DE INVENTARIO Y EQUIPO ---
    {"mensaje": "¡Habla serio! Se te rompió la armadura de la risa.", "comandos": ["clear @a minecraft:iron_chestplate", "clear @a minecraft:diamond_chestplate", "playsound minecraft:entity.item.break master @a"]},
    {"mensaje": "¡Qué nota! El Director tiene hambre y se comió tu almuerzo.", "comandos": ["clear @a minecraft:cooked_beef", "clear @a minecraft:golden_apple", "effect give @a minecraft:hunger 15 5"]},
    {"mensaje": "¡A lo pobre! Tus flechas se volvieron palillos de dientes.", "comandos": ["clear @a minecraft:arrow", "playsound minecraft:block.wood.break master @a"]},
    {"mensaje": "¡Qué mambo! El Director te quitó las antorchas para que sufras en la oscuridad.", "comandos": ["clear @a minecraft:torch", "effect give @a minecraft:blindness 10 1"]},

    # --- NIVEL: COMBOS LETALES (ESTADOS) ---
    {"mensaje": "¡Combo Sádico! Levitación + Ceguera = ¡Buena suerte!", "comandos": ["effect give @a minecraft:levitation 5 5", "effect give @a minecraft:blindness 10 1"]},
    {"mensaje": "¡La Pálida Ecuatoriana! Náuseas + Debilidad + Hambre.", "comandos": ["effect give @a minecraft:nausea 15 1", "effect give @a minecraft:weakness 20 2", "effect give @a minecraft:hunger 20 2"]},
    {"mensaje": "¡Piel de Cristal! Debilidad extrema + Fatiga de picar.", "comandos": ["effect give @a minecraft:weakness 60 10", "effect give @a minecraft:mining_fatigue 60 5"]},
    {"mensaje": "¡Habla brother! Te puse el efecto 'Cucaracha': no puedes saltar ni correr.", "comandos": ["effect give @a minecraft:slowness 20 5", "effect give @a minecraft:jump_boost 20 200"]}
]

class PlayerState(BaseModel):
    jugador: str
    salud: float
    bioma: str
    dimension: Optional[str] = "world"
    inventario: List[str]

@app.post("/analyze")
async def analyze_player(state: PlayerState):
    try:
        config = types.GenerateContentConfig(
            system_instruction=SYSTEM_PROMPT,
            temperature=0.7,
            safety_settings=[types.SafetySetting(category=c, threshold="BLOCK_NONE") 
                            for c in ["HARM_CATEGORY_HARASSMENT", "HARM_CATEGORY_HATE_SPEECH", 
                                     "HARM_CATEGORY_SEXUALLY_EXPLICIT", "HARM_CATEGORY_DANGEROUS_CONTENT"]]
        )
        ctx = f"Jugador:{state.jugador}|Vida:{state.salud}|Dim:{state.dimension}|Inv:{state.inventario}"
        
        response = client.models.generate_content(model='gemini-3-flash-preview', contents=ctx, config=config)
        
        if not response.text:
            raise Exception("AI Response Empty")
            
        clean_json = response.text.replace("```json", "").replace("```", "").strip()
        data = json.loads(clean_json)
        logger.info(f"SENTENCIA IA: {data.get('mensaje')}")
        return data
        
    except Exception as e:
        import random
        logger.error(f"FALLO DE IA: {e} | ACTIVANDO MODO EMERGENCIA")
        fallback = random.choice(LOCAL_FALLBACKS)
        logger.info(f"SENTENCIA EMERGENCIA: {fallback.get('mensaje')}")
        return fallback
