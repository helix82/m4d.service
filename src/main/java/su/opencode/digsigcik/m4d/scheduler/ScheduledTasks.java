package su.opencode.digsigcik.m4d.scheduler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import su.opencode.digsigcik.m4d.service.M4dService;

import java.io.IOException;

@Slf4j
@AllArgsConstructor
@Service
public class ScheduledTasks{

    private M4dService m4dService;

    @Scheduled(fixedRateString = "${scheduler.interval}") //   // каждые 60 секунд
    @Async
    public void callUpdateRequestStatusM4dInRR() {
        log.info("Вызов обновления статусов запросов сторонних МЧД в РР МЧД");
        m4dService.updateRequestStatusM4dInRR();
    }

    @Scheduled(fixedRateString = "${scheduler.interval}", initialDelayString = "${scheduler.delay15}")  // с задержкой 15 сек
    @Async
    public void callUpdateStatusM4dInRR() {
        log.info("Вызов обновления статусов МЧД ПЭП в РР МЧД");
        m4dService.updateStatusM4dInRR();
    }

    @Scheduled(fixedRateString = "${scheduler.interval}", initialDelayString = "${scheduler.delay30}")  // с задержкой 30 сек
    @Async
    public void callSendM4dRegisterInRr() throws IOException {
        log.info("Вызов отправки МЧД на регистрацию в РР МЧД");
        m4dService.sendM4dAllRegisterInRr();
    }

    @Scheduled(fixedRateString = "${scheduler.interval}", initialDelayString = "${scheduler.delay45}")  // с задержкой 30 сек
    @Async
    public void callSendM4dRevokeRegisterInRr() throws IOException {
        log.info("Вызов отправки заявлений на отзыв МЧД на регистрацию в РР МЧД");
        m4dService.sendM4dRevokeAllRegisterInRr();
    }
}
