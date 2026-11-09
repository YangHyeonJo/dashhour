# DASHHOUR (대시아워)

**내비게이션이 알려주지 않는 '도착 후 차에서 내리기까지의 시간'을 예측하고, 먼저 다녀간 운전자들의 주차 요령을 필요한 순간에 알려주는 Pleos Connect 차량 앱**

- 국문: '**대**기 **시**간 **아**까**워**'
- 현대자동차그룹 차량용 앱 개발 공모전 출품작

## 해결하는 문제
내비게이션의 도착 예정 시간은 목적지 입구에서 끝납니다. 주말 대형 복합몰은 도로 30분 거리라도 주차장 진입에만 30분 이상 걸리기도 하고, 넓은 주차장은 어디에 대느냐에 따라 걷는 시간이 크게 달라집니다. DASHHOUR는 이 '보이지 않는 이동 시간'을 차량 데이터와 운전자들의 경험으로 채웁니다.

## 왜 스마트폰·범용 AI가 아니라 플레오스인가
- **차량만 아는 신호**: 스마트폰은 주차가 끝난 순간을 모릅니다. 차량은 기어 P 신호로 정확히 압니다.
- **글로 쓰인 적 없는 지식**: "토요일엔 후문 진입이 빠르다" 같은 요령은 웹에 없고 운전한 사람의 경험에만 있습니다. 범용 AI는 답할 수 없고, 차량은 운전 과정에서 저절로 모읍니다.
- **플레오스는 다리**: 차 신호와 운전자의 한마디를 데이터로 바꾸고(Vehicle SDK·P단 음성), 다음 운전자의 차 화면·음성으로 돌려줍니다.
- **현대차만 가능한 규모**: 같은 주차장에 오는 차의 대부분이 같은 플랫폼을 쓰는 현대차그룹 차량이라, 같은 기준으로 빠르게 측정 데이터가 쌓입니다.

## 주요 기능
| 기능 | 설명 | 차량 특화 요소 |
|---|---|---|
| 진짜 도착시간 예측 | 내비 시간 + 장소·요일·시간대별 주차 진입 대기 = 차에서 내리는 시각 | 목적지 반경 진입부터 기어 P까지 자동 측정 |
| 주차 암묵지 안내 | 진입 경로, 층·구역, 출차 요령을 상황별로 한 번씩 안내 | 주행 중엔 음성만, 저속·정차 시 화면 표시 |
| P단 음성 팁 | 주차 직후 한마디로 팁 등록, 유형 자동 분류 | P단에서만 입력, 실제 주차 기록이 있어야 작성(방문 인증) |
| 평가·리워드 | 도움됨 평가, 측정·팁 참여 리워드 | 플레오스 서비스와 연계 |

## 동작 흐름
홈 → 진짜 도착시간(출발 전) → 주행 중 → 목적지 1km 전 음성 안내 → 주차장 진입 팁 → 기어 P 주차 완료·음성 팁 → 기어 D 출차 팁

## 구조
- **화면·로직**: `app/src/main/assets/web/index.html` (웹뷰) — 주행 상태 머신, 도착시간 계산, 팁 선택
- **네이티브**: `MainActivity.kt` (웹뷰·브리지), `vehicle/PleosGearSource.kt` (Vehicle SDK 기어 구독), `TtsSpeaker.kt` (음성 안내)

## 사용 SDK
| SDK | 용도 | 상태 |
|---|---|---|
| Vehicle SDK 2.3.1 | 기어 P/D 신호로 주차 완료·출차 감지 | 연동 (에뮬레이터 확인 완료) |
| 안드로이드 TextToSpeech | 음성 안내 | Pleos TTS SDK 정식 제공 시 교체 예정 |
| NaviHelper, FusedLocation, SpeechToText | 목적지·위치·음성 입력 | 데모에서는 시나리오 데이터로 대체 |

## 설치와 테스트 (심사용)
1. Pleos Connect Emulator v2.1.0 (API 34)을 실행한다.
2. [`apk/DASHHOUR_DASHHOUR_release.apk`](../apk/DASHHOUR_DASHHOUR_release.apk)를 받아 에뮬레이터 화면에 끌어다 놓는다.
3. 앱 목록에서 DASHHOUR를 열고, 오른쪽 아래 **데모** → 신호 출처 **차량 (Pleos)** → 시나리오 **A**를 고른다.
4. Virtual Console에서 기어 **D → P(5초) → D** 순서로 바꾸면 주차 완료(27분 측정)와 출차 팁이 나온다.

자세한 절차와 기대 결과: [테스트 시나리오](docs/TEST_SCENARIO.md)

## 문서
- [테스트 시나리오](docs/TEST_SCENARIO.md)
- [개발기획서](docs/DASHHOUR_dev_spec.docx)
- 아키텍처: [비즈니스](docs/architecture/1_business.png) · [데이터](docs/architecture/2_data.png) · [인프라](docs/architecture/3_infra.png) · [차별성](docs/architecture/4_differentiation.png)

![비즈니스 아키텍처](docs/architecture/1_business.png)

## 빌드
- Android Studio, Android SDK 35, JDK 17, minSdk 28
- Pleos Connect Emulator v2.1.0 (API 34, Automotive)
- 앱 우측 하단 '데모' 버튼으로 시나리오 재생·기어 조작 콘솔을 엽니다.

© 2026 DASHHOUR. All rights reserved. 공모전 출품작으로 무단 사용·배포를 금합니다.
