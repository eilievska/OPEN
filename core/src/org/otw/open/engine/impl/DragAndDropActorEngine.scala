package org.otw.open.engine.impl

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.{Payload, Source, Target}
import com.badlogic.gdx.scenes.scene2d.{InputEvent, Stage}
import com.badlogic.gdx.utils.viewport.StretchViewport
import org.otw.open.controllers.{CauseAndEffectFinishedSuccessfully, CauseAndEffectFinishedUnsuccessfully, ScreenController}
import org.otw.open.dto.Drawing
import org.otw.open.engine.Engine
import org.otw.open.engine.actor.DragAndDropActor

/**
  * Created by smirakovska on 2/9/2016.
  *
  * @param theme             - name of theme
  * @param imgBackgroundPath - path to background image file
  */
class DragAndDropActorEngine(val theme: String, val imgBackgroundPath: String) extends Engine {

  /** Stage object */
  val stage: Stage = new Stage(new StretchViewport(1440, 900))

  /** Mutable instance of DragAndDropActor */
  private var actor = new DragAndDropActor(theme, imgBackgroundPath, 0, 320)

  val dragAndDrop = new DragAndDrop()

  /** Sound instance for audio guidance */
  private val audioGuidance: Music = Gdx.audio.newMusic(Gdx.files.internal("audioGuidanceDragAndDrop.mp3"))
  audioGuidance.play

  /** animation object sound */
  val animationObjectSound: Music = Gdx.audio.newMusic(Gdx.files.internal("carEngine.mp3"))

  /** Number of failed attempts */
  private var failedAttempts = 0

  /** Number of maximum allowed failed attempts */
  private val maxFailedAttempts = 3

  Gdx.input.setInputProcessor(stage)

  /** adding source actor */
  dragAndDrop.addSource(new Source(actor) {
    val payload: Payload = new Payload

    override def dragStart(event: InputEvent, x: Float, y: Float, pointer: Int): Payload = {
      animationObjectSound.setLooping(true)
      animationObjectSound.play
      payload.setObject(actor)
      payload.setDragActor(actor)
      payload.setInvalidDragActor(actor)
      payload
    }

    override def dragStop(event: InputEvent, x: Float, y: Float, pointer: Int, payload: Payload,
                          target: Target) {
      if (target == null) {

        actor.resetPosition
        failedAttempts += 1
      }
      stage.addActor(actor)
      if (failedAttempts == maxFailedAttempts) {
        ScreenController.dispatchEvent(CauseAndEffectFinishedUnsuccessfully)
      }
    }
  })

  /**
    * adding target object
    */
  dragAndDrop.addTarget(new Target(actor) {

    def drag(source: Source, payload: Payload, x: Float, y: Float, pointer: Int): Boolean = {
      dragAndDrop.setDragActorPosition(-216, 216)
      true
    }

    def drop(source: Source, payload: Payload, x: Float, y: Float, pointer: Int) {
      animationObjectSound.stop
      if (source.getActor().getX() >= 900
        && source.getActor().getX() <= 1100
        && source.getActor().getY() >= 259
        && source.getActor().getY() <= 452) {
        actor = new DragAndDropActor(theme, imgBackgroundPath, source.getActor.getX(), source.getActor.getY)
        ScreenController.dispatchEvent(CauseAndEffectFinishedSuccessfully)
      }
      else {
        actor.resetPosition
        failedAttempts += 1
      }
    }
  })

  /**
    * add actor to the scene
    */
  stage.addActor(actor)

  override def dispose(): Unit = {
    actor.dispose()
    audioGuidance.dispose()
    animationObjectSound.dispose()
  }

  override def getDrawings(delta: Float): List[Drawing] = List.empty

  /**
    *
    * @return an option of stage. Returns Some[Stage] in the case when the engine uses a stage with actors.
    */
  override def getStage(): Option[Stage] = Some(stage)
}
