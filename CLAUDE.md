# DASHHOUR — Claude Code 작업 안내

## 프로젝트
- 현대자동차그룹 차량용 앱 개발 공모전(Pleos Connect) 출품작. 접수 2026-11-15, 2차 PT 2026-12-03~04 (실제 동작하는 앱 필수).
- 한 줄 소개: 내비가 알려주지 않는 "도착 후 차에서 내리기까지의 시간"(주차 진입 대기 등)을 예측하고, 먼저 다녀간 운전자들의 주차 요령(암묵지)을 필요한 순간에 알려주는 차량 앱.
- 상세 요구사항: `docs/DASHHOUR_dev_spec.docx` (화면 SCR-01~07, 상태 머신, 계산식, 팁 선택 규칙, 완료 기준 TC-01~09).

## 구조 (하이브리드: 웹뷰 + 네이티브 브리지)
- `app/src/main/assets/web/index.html` — 화면 전체, 주행 상태 머신, 진짜 도착시간 계산, 팁 선택, 샘플 데이터, 데모 시나리오. **앱의 기능은 대부분 여기 있다.**
- `MainActivity.kt` — 웹뷰를 띄우고 기어 신호·상태를 JS로 전달 (`window.dashhour.vehicle({gear})`, `window.dashhour.vehicleStatus(text)`).
- `NativeBridge.kt` — JS에서 `window.DashNative.speak(text)` 등으로 호출하는 기능.
- `TtsSpeaker.kt` — 안드로이드 기본 TTS (Pleos TextToSpeech SDK는 아직 "준비 중"이라 대체).
- `vehicle/PleosGearSource.kt` — Vehicle SDK `Gear` 도메인을 구독 (리스너는 `GearListenerAdapter.java`).

## 상태 머신 (기획서 6장)
IDLE → PLANNED(목적지 설정) → DRIVING(기어 D + 차속) → APPROACHING(남은 거리 ≤ 1km, 진입 경로 팁 음성)
→ ENTERING(≤ 200m + ≤ 15km/h 3초) → PARKED(기어 P 5초 유지, 소요시간 기록 +10P, 음성 팁)
→ EXITING(기어 D/R, 출차 팁) → IDLE

## 신호 모드
- **시나리오**: 거리·차속·기어를 스크립트로 재생 (기본값, 실차 없이 시연 가능).
- **차량 (Pleos)**: 거리·차속은 시나리오, 기어는 Vehicle SDK. Virtual Console에서 D → (주차장 도착 후) P → D 순서로 바꾸면 화면이 넘어간다. 기어 연결에 성공하면 자동으로 이 모드가 된다.
- **Virtual Console 여는 법**: 에뮬레이터 안에서 8088 포트 웹페이지로 돈다. `adb forward tcp:8088 tcp:8088` 후 PC 브라우저에서 `http://localhost:8088` → P R N D 버튼. 에뮬레이터를 재시작하면 forward를 다시 해야 한다. (`cmd car_service inject-vhal-event`는 user 빌드라 막힘)
- 앱 우측 하단 '데모' 버튼으로 콘솔(시나리오 선택, 재생 속도, 기어 수동 조작, 신호 출처)을 연다.

## 작업 원칙
- 데모 모드는 항상 동작해야 한다. SDK 연결이 실패해도 크래시 없이 시나리오 모드로 남는다.
- 주행 중(차속 > 0)에는 터치 입력을 받지 않고 음성·자막만 쓴다. 입력은 P단에서만.
- 좌표·이동 경로는 저장하지 않는다. 장소 ID 단위로만 기록.
- 화면 문구·시나리오 수치(타임스퀘어: 도로 30분, 진입 25분, 15:25, 측정 27분)는 기획서·시안과 맞춘다.
- 웹 화면은 1600×720 기준으로 그리고 화면 크기에 맞춰 비율 유지 축소된다.

## 빌드·실행
1. Android Studio로 이 폴더를 연다 (Gradle 동기화 시 Pleos 저장소 nexus-playground.pleos.ai 접근 필요).
2. Pleos Connect Emulator v2.1.0(API 34, Automotive) 실행 후 Run.
3. 로그 확인: Logcat 태그 `DashHour`, `DashGear`, `DashTts`. 웹 화면 디버깅은 PC 크롬 `chrome://inspect`.

## TODO (우선순위 순)
1. [x] **빌드 확인** (11/6): Vehicle SDK는 **2.3.2** (2.3.1은 저장소에 없음, 404). Gradle JVM은 `~/.jdks/jbr-21.0.11`. AS 내장 jbr(25)로는 Gradle 8.10.2가 안 돈다.
2. [x] **기어 API 확정** (11/6): 기어는 DrivingMode가 아니라 `Vehicle.getGear()` → `Gear.getCurrentGearSelectionState(onComplete, onFailed)` / `registerGearSelection(GearSelectionListener)`이고, 값은 `VehicleGear.GEAR_PARK/…`이다. 권한은 `pleos.car.permission.CAR_DRIVE` (부여 확인). 리스너를 Kotlin으로 직접 구현하면 "overrides nothing" 컴파일 오류가 나서 `vehicle/GearListenerAdapter.java`로 감쌌다. SDK에는 deprecated 표시("use the Google GEAR_SELECTION property")가 있지만 동작한다.
3. [x] **Virtual Console 시연 확인** (11/6, 직접 시연으로 통과 확인. 기어 실시간 수신은 로그로 확인): 기어 D → P(5초) → D로 TC-04(P 5초 미만이면 전환 안 됨) 통과 확인.
4. [ ] **TTS 확인** (11/6: `TTS init failed: -1`로, 엔진 자체가 없음): 에뮬레이터에 한국어 TTS가 없으면 `DashTts` 로그에 ready=false. 이 경우 자막만으로 시연.
5. [ ] **NaviHelper**: 목적지·ETA 조회가 되면 홈 대신 내비 목적지로 SCR-02를 바로 띄우기 (FR-01-05).
6. [ ] **화면 해상도**: 에뮬레이터 실제 해상도에서 글자 크기·여백 점검.
7. [ ] 웹 폰트는 인터넷이 없으면 기본 한글 폰트로 표시된다. 필요하면 폰트 파일을 assets에 넣어 로컬로 로드.
