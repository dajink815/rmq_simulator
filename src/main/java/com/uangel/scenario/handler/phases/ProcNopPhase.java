package com.uangel.scenario.handler.phases;

import com.uangel.media.rtp.MediaPlayer;
import com.uangel.media.rtp.PcapAudioPlayer;
import com.uangel.media.rtp.WavPlayer;
import com.uangel.model.SessionInfo;
import com.uangel.scenario.phases.MsgPhase;
import com.uangel.scenario.phases.NopPhase;
import com.uangel.scenario.phases.actions.ActionPhase;
import com.uangel.scenario.phases.actions.ExecNode;

import java.util.Optional;

/**
 * @author dajin kim
 */
public class ProcNopPhase extends ProcMsgPhase {

    public ProcNopPhase(SessionInfo sessionInfo) {
        super(sessionInfo);
    }

    @Override
    public void run(MsgPhase msgPhase) {
        NopPhase phase = (NopPhase) msgPhase;
        for (ActionPhase action : phase.getActions()) {
            actionRun(action);
        }
        sessionInfo.execPhase(sessionInfo.increaseCurIdx());
    }

    public void actionRun(ActionPhase action) {
        for (ExecNode exec : action.execs) {
            if (exec.rtpStream != null){
                execRtpStream(exec);
            } else if(exec.playPcapAudio != null){
                execPlayPcap(exec);
            }
        }
    }

    public void execRtpStream(ExecNode exec){
        if (exec.rtpStream.equals("pause")) {
            Optional.ofNullable(sessionInfo.getMediaPlayer()).ifPresent(MediaPlayer::pause);
        } else if (exec.rtpStream.equals("resume")) {
            Optional.ofNullable(sessionInfo.getMediaPlayer()).ifPresent(MediaPlayer::resume);
        } else if (exec.rtpStream.equals("stop")) {
            Optional.ofNullable(sessionInfo.getMediaPlayer()).ifPresent(mediaPlayer -> mediaPlayer.stop("Scenario Stop"));
        } else {
            Optional.ofNullable(sessionInfo.getMediaPlayer()).ifPresent(mediaPlayer -> mediaPlayer.stop("Scenario Stop To Play"));
            MediaPlayer mediaPlayer = new WavPlayer(scenario.getScenarioRunner(), sessionInfo.getSessionId(), exec.rtpStream);
            sessionInfo.setMediaPlayer(mediaPlayer);
            mediaPlayer.play(sessionInfo.getMediaInfo().getLocalAudioAddr().getPort(), sessionInfo.getMediaInfo().getRemoteAudioAddr());
        }
    }

    public void execPlayPcap(ExecNode exec){
        Optional.ofNullable(sessionInfo.getMediaPlayer()).ifPresent(mediaPlayer -> mediaPlayer.stop("Scenario Stop To Play"));
        MediaPlayer mediaPlayer = new PcapAudioPlayer(scenario.getScenarioRunner(), sessionInfo.getSessionId(), exec.playPcapAudio);
        sessionInfo.setMediaPlayer(mediaPlayer);
        mediaPlayer.play(sessionInfo.getMediaInfo().getLocalAudioAddr().getPort(), sessionInfo.getMediaInfo().getRemoteAudioAddr());
    }
}
