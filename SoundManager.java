import java.io.IOException;
import javax.sound.sampled.*;

/** Holds all sounds */
public class SoundManager {
  private static final String twangFilename = "twang.wav";

  SoundManager() {
   
  }

  void playTwang() {
    try {
      Clip sound = AudioSystem.getClip();
      sound.open(AudioSystem.getAudioInputStream(getClass().getResource(twangFilename)));
      sound.start();
    } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
      // ignore
    }
  }
}