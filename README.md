# 📱 Android Приложение с 6 Основными Экранами

## 📖 Описание

Это Android-приложение, сочетающее в себе музыкальный плеер, поиск фильмов, прогноз погоды и чат. Основной упор сделан на кастомную навигацию, мультиязычность, адаптацию под требования ревьюеров и анимации переходов.
---
---

## 🏠 Экран "кнопочный рай"

<table width="980">
  <tr>
    <td width="160" align="center" valign="middle">
      <img src="https://github.com/user-attachments/assets/24b37909-0025-4e75-8f91-34ee00739979" alt="left" width="100">
    </td>
    <td width="530" valign="middle" align="left">
      <p><b>Кнопочный экран</b></p>
      <p>
        • 6 кнопок (3+3)<br>
        • Переключение по заголовку<br>
        • Тулбар + <code>Dropdown</code><br>
        • EN: Фильтр и Dropdown ➕ добавлены 🙂 эмодзи для интуитивности 🌼<br>
        • RU: Фильтр и Dropdown «Ромашка» 🌼 <br>
        • Без <code>back stack</code>, 6 анимаций  <br>
        • На экране кнопок без нижней навигации  😕 (т.к. нижняя навигация его альтернатива).
      </p>
    </td>
    <td width="160" align="center" valign="middle">
      <img src="https://github.com/user-attachments/assets/6b507d25-5873-4970-b4f6-c1948a4d80ce" alt="right-1" width="100">
    </td>
    <td width="160" align="center" valign="middle">
      <img src="https://github.com/user-attachments/assets/b12b93f1-aac3-473a-8398-bd0dcbd1c661" alt="right-2" width="100">
    </td>
  </tr>
</table>

<table width="100%">
  <tr>
    <td align="center" width="25%"><img src="https://github.com/user-attachments/assets/9312c17e-ce5d-4b63-b827-880d9ec82fcd" alt="shot-1" width="220"></td>
    <td align="center" width="25%"><img src="https://github.com/user-attachments/assets/7d1bcd51-8ee0-4c79-8ee4-2dc727cb96f0" alt="shot-2" width="220"></td>
    <td align="center" width="25%"><img src="https://github.com/user-attachments/assets/3226ee99-f29b-4f6f-8e9d-6a8a541a5537" alt="shot-3" width="220"></td>
    <td align="center" width="25%"><img src="https://github.com/user-attachments/assets/13e3382b-f7f3-42fd-80f0-50667b72381a" alt="shot-4" width="220"></td>
  </tr>
</table>

---

<h2 align="right">🔽 🚗 Нижний навигатор</h2>

<table width="980">
  <tr>
    <td width="160" align="center" valign="middle">
      <img src="https://github.com/user-attachments/assets/e8cbca1e-2b60-41c9-8ca9-f4d654cd1b09" alt="left" width="100">
    </td>
       <td width="160" align="center" valign="middle">
      <img src="https://github.com/user-attachments/assets/ae972525-699c-4d6e-af6e-4fcea2de05dc" alt="left" width="100">
    </td>
       <td width="160" align="center" valign="middle">
      <img src="https://github.com/user-attachments/assets/ba4534a0-049b-4167-9d0d-336b8dbf6a99" alt="left" width="100">
    </td>
    <td width="530" valign="middle" align="left">
      <p align="right"><b>Нижняя навигация</b></p>
      <p>
        • Отображается на 6 внутренних экранах.<br>
        • Каждый экран — 3 кнопки навигации.<br>
        • Синий цвет — активный экран, нажатие на него открывает вторую тройку кнопок.<br>
        • Переходы прямые, без stack, и с кастомной анимацией.<br>
        • Нажатие "заглавия" в тулбаре возвращает на первый набор по 3 кнопки из главного меню. <br>
      </p>
    </td>
    <td width="160" align="center" valign="middle">
      <img src="https://github.com/user-attachments/assets/7fcfde90-491c-45a1-ba2a-0d3bc85c5b5f" alt="right-1" width="100">
    </td>
      <td width="160" align="center" valign="middle">
      <img src="https://github.com/user-attachments/assets/0c2a5d91-f937-4cd0-ab14-0d84671cf297" alt="right-1" width="100">
    </td>
  
  </tr>
</table>

---

## 🔍 Экран Поиска музыки

<table width="980">
  <tr>
    <td width="160" align="center" valign="middle">
      <img src="https://github.com/user-attachments/assets/c1af9417-17b9-43e2-b05c-a4b300ebbd99" alt="left" width="100">
    </td>
      <td width="160" align="center" valign="middle">
      <img src="https://github.com/user-attachments/assets/91bb0865-8f19-4589-9a40-962b60aea959" alt="left" width="100">
    </td>
    <td width="530" valign="middle" align="left">
      <p><b>Поиск музыки</b></p>
      <p>
        • Хранит историю последних **10 треков** ➕ обновление на верхнюю строку в истории<br>
        • При переходе на трек — по умолчанию **горизонтальный скрол** всех треков из результата поиска.<br>
        • При клике на трек — переключение на **вертикальный скрол**<br>
        • Горизонтальный и вертикальные скролы реализованы на базе RecyclerView ➕ альтернатива на базе ViewPager2 в экране Кино<br>          
      </p>
    </td>
    <td width="160" align="center" valign="middle">
      <img src="https://github.com/user-attachments/assets/b000407e-cee0-4ede-89ae-7c4a8071ec2e" alt="right-1" width="100">
    </td>
    <td width="160" align="center" valign="middle">
      <img src="https://github.com/user-attachments/assets/d6c03076-1670-4b34-8275-f234d7e4be54" alt="right-2" width="100">
    </td>
       <td width="160" align="center" valign="middle">
      <img src="https://github.com/user-attachments/assets/08e9b02e-a7d3-48f7-8f3d-3e5845ea008d" alt="right-2" width="100">
    </td>
  </tr>
</table>

<table width="100%">
  <tr>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/aa9dc933-1053-40f1-a41b-00f2b69ba5fc" alt="shot-1" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/de0eb6ad-d78f-4be5-a005-e12b3667e576" alt="shot-2" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/a1906c02-0f9f-4065-a772-9732c7ff32f8" alt="shot-3" width="220"></td>
      <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/fc14cc18-c0c3-4aa3-b5ce-e840333bef0f" alt="shot-3" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/67ba409e-09c3-43b7-bd2a-e1e8cedb0cf6" alt="shot-4" width="220"></td>
  </tr>
</table>

<table width="980">
  <tr>
    <td width="160" align="center" valign="middle">
      <img src="https://github.com/user-attachments/assets/8d6670bf-0d72-4abf-95b2-ebd575c038f2" alt="left" width="100">
    </td>
      <td width="160" align="center" valign="middle">
      <img src="https://github.com/user-attachments/assets/91bb0865-8f19-4589-9a40-962b60aea959" alt="left" width="100">
    </td>
    <td width="530" valign="middle" align="left">
      <p>
        • Удаление треков из истории и списка — по **нажатию на стрелку**<br>
        • Варианты отображения:<br>
        - Верхний тулбар (всегда)<br>
        - Нижний навигатор (список пуст, динамика)<br>
        - Альтернативное исполнение: Автоматическая прозрачность на экране "Кино" при не пустом списке <br>         
      </p>
    </td>
    <td width="160" align="center" valign="middle">
      <img src="https://github.com/user-attachments/assets/621a4bfe-25b9-4839-9ed5-017411b6ba7e" alt="right-1" width="100">
    </td>
    <td width="160" align="center" valign="middle">
      <img src="https://github.com/user-attachments/assets/d6c03076-1670-4b34-8275-f234d7e4be54" alt="right-2" width="100">
    </td>
       <td width="160" align="center" valign="middle">
      <img src="https://github.com/user-attachments/assets/08e9b02e-a7d3-48f7-8f3d-3e5845ea008d" alt="right-2" width="100">
    </td>
  </tr>
</table>

<table width="980">
  <tr>
    <td width="160" align="center" valign="middle">
      <img src="https://github.com/user-attachments/assets/8d6670bf-0d72-4abf-95b2-ebd575c038f2" alt="left" width="100">
    </td>
      <td width="160" align="center" valign="middle">
      <img src="https://github.com/user-attachments/assets/cd6cc41c-ea41-4fe4-92d3-649f2403d50f" alt="left" width="100">
    </td>
       <td width="160" align="center" valign="middle">
      <img src="https://github.com/user-attachments/assets/894a8506-d930-492a-9696-60876316d1e9" alt="left" width="100">
    </td>
    <td width="530" valign="middle" align="left">
      <p>
        • Переход на **экран AudioPlayer**<br>
        - Расширенные опции: смена темы, поделиться треком (в просмотре отдельного экрана) или треками(с экрана поиска-всеми треками из истории)<br>
        - При отправке трек оформление с именем исполнителя и названием трека(ов)<br>
        - Поддержка переключения темы, шаринга  🎵 трека или 🎶 треков, смены цветов текста, кнопок и фона, все функции настроек с 🌼 тулбара <br>
        - При импорте 🎵 трека или 🎶 треков поддреживается как вертикальный так и горизонтальный скол<br> 
      </p>
    </td>
    <td width="160" align="center" valign="middle">
      <img src="https://github.com/user-attachments/assets/be83e64e-bf97-4c77-abf3-3c5248ae2ffc" alt="right-1" width="100">
    </td>
    <td width="160" align="center" valign="middle">
      <img src="https://github.com/user-attachments/assets/fa691ba3-bbaa-4178-9bda-2e31ec7d6402" alt="right-2" width="100">
    </td>
  </tr>
</table>

<table width="980">
  <tr>
    <td width="160" align="center" valign="middle">
      <img src="https://github.com/user-attachments/assets/a48b3693-2907-4b5c-a824-eabfc87bf7c7" alt="left" width="100">
    </td>
    <td width="530" valign="middle" align="left">
      <p>
        - Включении трека работает 🕒 трека и кнопки play, pause, favorite, share<br>
        - Просмотр отдельного трека, реализация ландскейпа, поддержка размеров на sw320dp, sw360dp, sw393dp<br>
        - При отправке трек оформление с именем исполнителя и названием трека(ов)<br>
        - В экране поиска длина названия или исполнителя трека ограничена в одну строку, тогда как при отдельном просмотре нет ограничений, доп. в ландскейпе скрол для теста<br>
      </p>
    </td>
    <td width="160" align="center" valign="middle">
      <img src="https://github.com/user-attachments/assets/f130505e-aed3-4853-a18e-fb9dd992572f" alt="right-1" width="100">
    </td>
    <td width="160" align="center" valign="middle">
      <img src="https://github.com/user-attachments/assets/aa788da1-06a8-448a-9739-d990c8cd5ec3" alt="right-2" width="100">
    </td>
       <td width="160" align="center" valign="middle">
      <img src="https://github.com/user-attachments/assets/110bab49-c0c0-4b35-80d7-fba86f5921af" alt="right-2" width="100">
    </td>
  </tr>
</table>

<table width="100%">
  <tr>
    <td align="center" width="33%"><img src="https://github.com/user-attachments/assets/44d8c088-a982-4def-89fd-fd21139f1689" alt="shot-1" width="320"></td>
    <td align="center" width="33%"><img src="https://github.com/user-attachments/assets/de7cf965-ee40-4620-9533-016c327fc1a8" alt="shot-2" width="320"></td>
    <td align="center" width="33%"><img src="https://github.com/user-attachments/assets/e76aa28d-c77f-4d45-8afd-3d9a139b913f" alt="shot-3" width="320"></td>
  </tr>
</table>

<table width="980">
  <tr>
      <td width="160" align="center" valign="middle">
      <img src="https://github.com/user-attachments/assets/861fb744-0790-4bab-b367-03c0a8eea1b6" alt="left" width="100">
    </td>
    <td width="530" valign="middle" align="left">
      <p>
        - DropDown 🌼 тулбара в виде вложенных кругов вращаются относительно центрального круга для расширения списка <br>
        - Переворот списка поиска (reverse scroll) — по фильтру тулбара, оповещение статуса связи <br> 
      </p>
    </td>
       <td width="530" valign="middle" align="left">
      <p>
        - Альтернативное бесплатная авто-поиск видео режима песен с тулбара + при остутсвии видео полное звуковая поддержка, при видео авто полно-экранное на ландскейпе, time bar для прокрутки назад-вперед<br>   
      </p>
     </td>
    <td width="160" align="center" valign="middle">
      <img src="https://github.com/user-attachments/assets/612b7d6a-0689-46f0-9d54-11934c84d525" alt="right-1" width="100">
    </td>
  </tr>
</table>

---

<h2 align="right">❤️ Медиатека 📝</h2>

<table width="980">
  <tr>
     <td width="250" align="center" valign="top">
      <table width="100%" cellpadding="0" cellspacing="0">
        <tr><td align="center"><img src="https://github.com/user-attachments/assets/23fbc39d-c334-4f63-a413-a223d4b620f7" alt="left-1" width="250"></td></tr>
        <tr><td height="6"></td></tr>
        <tr><td align="center"><img src="https://github.com/user-attachments/assets/cbbc6586-a4df-4245-893d-35e6a564efed" alt="left-2" width="250"></td></tr>
      </table>
    </td>
       <td width="530" valign="middle" align="left">
      <p>
        ❤️ Избранное <br>
        • Добавление треков в избранное <br>
        • Просмотр списка любимых композиций <br>
        • Локальное хранение избранных треков <br> 
        • Управление цветами <br> 
        📝 Создание плейлистов  <br>
        • Создание пользовательских плейлистов <br>
        • Добавление обложки, названия и описания <br>
        • Добавление треков в плейлисты <br>   
        • Управление плейлистами (редактирование, удаление) <br>
        • Импорт-экспорт плейлистами <br>
      </p>
     </td>
    <td width="300" align="center" valign="middle">
      <img src="https://github.com/user-attachments/assets/92c324cc-26bb-4a8c-b6aa-6b66749a7a77" alt="right-1" width="300">
    </td>
       </td>
    <td width="300" align="center" valign="middle">
      <img src="https://github.com/user-attachments/assets/1a6aa1c9-4c3f-4333-b7df-7744770d846a" alt="right-1" width="300">
    </td>
  </tr>
</table>

<table width="100%">
  <tr>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/b2676c78-ecf8-4dee-8179-8b61533e0af1" alt="shot-1" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/bfaf46cf-cf7e-4209-b5eb-0af8a1928d56" alt="shot-2" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/4bd3766a-9ca6-46cc-9960-51ef0b2032bc" alt="shot-3" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/97da4863-ecfe-4114-a303-a6c0f558bd6b" alt="shot-3" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/3ce74af0-89a2-4708-87ae-a562407d206d" alt="shot-3" width="200"></td>
  </tr>
</table>

<table width="100%">
  <tr>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/e1da4b8c-2e84-459e-b06d-81bbd034fcbf" alt="shot-1" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/22ac602c-5d0d-42c3-b12a-a271e2b32fa8" alt="shot-2" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/4185fce3-e143-4329-8b40-bc126f3be6aa" alt="shot-3" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/fcff25eb-5825-4a03-bbf6-9ff3b16f57ae" alt="shot-3" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/94666179-96e7-4484-a228-b266351f3376" alt="shot-3" width="200"></td>
  </tr>
</table>


<table width="100%">
  <tr>
    <td align="center" width="33%"><img src="https://github.com/user-attachments/assets/54eaa38c-92c8-4474-91de-3d43ef62fe47" alt="shot-1" width="320"></td>
    <td align="center" width="33%"><img src="https://github.com/user-attachments/assets/16a34c77-5704-4128-9509-2848d123bca8" alt="shot-2" width="320"></td>
    <td align="center" width="33%"><img src="https://github.com/user-attachments/assets/c58ff4d2-783d-4bee-bcfe-ca5efb4ed601" alt="shot-3" width="320"></td>
  </tr>
</table>

<table width="100%">
  <tr>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/73bd56cd-c144-4e0a-b9f7-0225357d7df9" alt="shot-1" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/5fcf22c6-da64-4c0c-acac-946e1ab61136" alt="shot-2" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/2c2123a8-b849-4918-a3fc-ed53651ca9ce" alt="shot-3" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/4895a7ac-fc1e-4ccc-a60c-6f35a28188e7" alt="shot-3" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/e04e1e0a-2c0f-4ebb-a8ab-18e25f21c574" alt="shot-3" width="200"></td>
  </tr>
</table>

<table width="100%">
  <tr>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/0d499099-afc6-4124-8ac5-6d70a17f1a0e" alt="shot-1" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/61304bb8-b72c-4ef3-9265-9f8791ac5565" alt="shot-2" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/b4575640-1f43-4535-a7a8-7a8da1dbb04c" alt="shot-3" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/ea4de33d-04f2-444f-9d7b-7c6bf8621010" alt="shot-3" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/81c12fa7-8b74-402c-a82e-aec0bbab2fbf" alt="shot-3" width="200"></td>
  </tr>
</table>

---

<h2 align="right"> ⚙️ Экран Настроек</h2>

<table width="100%">
  <tr>
    <td align="center" width="25%"><img src="https://github.com/user-attachments/assets/0bc7261a-ffbb-495d-a9a2-95366d470bde" alt="shot-1" width="230"></td>
    <td align="center" width="25%"><img src="https://github.com/user-attachments/assets/66ee37a5-d66f-4bd7-8cc8-cca5f8603534" alt="shot-2" width="230"></td>
    <td align="center" width="25%"><img src="https://github.com/user-attachments/assets/b43f519d-3b59-4433-b3d4-7d31b31a63f6" alt="shot-3" width="230"></td>
    <td align="center" width="25%"><img src="https://github.com/user-attachments/assets/29165c11-0813-4f71-85bc-bc4c73fe5bfd" alt="shot-3" width="230"></td>
  </tr>
</table>

<table width="100%">
  <tr>
    <td align="center" width="25%"><img src="https://github.com/user-attachments/assets/a3b61ffe-ac44-4d80-b3fe-c16004293d8f" alt="shot-1" width="230"></td>
       </td>
       <td width="230" valign="middle" align="left">
      <p>
        - Смена темы (глобально или через тулбар) <br>
        - Соглашение 📝, поддержка 🐶, поделиться 💌 — доступны через тулбар (`BaseActivity`) <br>
      </p>
     </td>
    <td align="center" width="25%"><img src="https://github.com/user-attachments/assets/7e69953e-664c-4e57-ba33-90d6e6d5c1a3" alt="shot-2" width="230"></td>
    <td align="center" width="25%"><img src="https://github.com/user-attachments/assets/85822c37-9b13-4f7f-b999-7a0d9039ee34" alt="shot-3" width="230"></td>
  </tr>
</table>

---

## 🎞️ Экран Кино



