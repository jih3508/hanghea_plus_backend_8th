package kr.hhplus.be.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "kr.hhplus.be.server.infrastructure")
@EnableCaching
public class ServerApplication {

	public static void main(String[] args) {
		try {
			// 클래스패스에서 리소스를 가져옴 (WAV, AIFF, AU 형식만 지원)
			InputStream inputStream = ServerApplication.class.getClassLoader().getResourceAsStream("Sahur.mp3");
			if (inputStream == null) {
				System.err.println("Sound file not found in resources");
			} else {
				// 오디오 입력 스트림 생성
				AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream);
				
				// 오디오 형식 가져오기
				AudioFormat format = audioInputStream.getFormat();
				
				// 데이터 라인 정보 생성
				DataLine.Info info = new DataLine.Info(Clip.class, format);
				
				// 클립 생성
				Clip clip = (Clip) AudioSystem.getLine(info);
				
				// 오디오 스트림을 클립에 열기
				clip.open(audioInputStream);
				
				// 재생
				clip.start();
				
				// 오디오가 끝날 때까지 대기 (선택 사항)
				Thread.sleep(clip.getMicrosecondLength() / 1000);
				
				// 자원 해제
				clip.close();
				audioInputStream.close();
			}
		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException | InterruptedException e) {
			System.err.println("Error playing sound: " + e.getMessage());
		} finally {
			SpringApplication.run(ServerApplication.class, args);
		}
	}
}
