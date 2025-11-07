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

## ⚙️ Экран Настроек

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
<h2 align="right">🎞️ Экран Кино</h2>

<table width="100%">
  <tr>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/1db1fcab-1401-4bb8-b01a-69013f802626e" alt="shot-1" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/a2b17d96-3ccc-49c0-962e-9bb777c1bd9e" alt="shot-2" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/30ba1f2d-92e4-41e0-9fd5-7a1321003153" alt="shot-3" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/6f7b8a4d-a990-48e2-b099-db0c16abe5be" alt="shot-3" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/16bd48b5-b07e-4465-bbc1-4e7d0115be4c" alt="shot-3" width="200"></td>
  </tr>
</table>

- Поиск фильмов:
    - Кнопка старта как альтернатива экрану поиска Песен.
    - Автостарт реализован (и закомментирован в коде).
- Результаты поиска при переходе на трек:
    - ViewPager2 с **горизонтальным скролом** (по умолчанию).
    - Переключение на **вертикальный скрол** — по кнопке.
- Верхний тулбар и нижний навигатор доступны.
- Деление фильмов на избранные (❤️).
- При поиске обьединяются и выдаются результы двух режимов поиска:
    - **Обычный** — имена актёров.
    - **Расширенный** — краткое описание фильма.
- При отдельном просмотре фильма как альтернатива экрану поиска песен — **без тулбара** и **нижнего навигатора**.
- Переворот списка (reverse scroll) — по фильтру.
- ✨📽️💃 Шаринг кинофильма при просмотре в режиме просмотра постера фильма
- Получение кинофильма и распознавание если она в вашем списке избранных
- Переход на фрашменты, где имеется детали фильма, список и имена звезд и их фотографии
- Экран поиск звезд(актеров и актрис) альтернативно создан из ресурсов Википедия, с определением языка из поля ввода поиска

<table width="100%">
  <tr>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/cdc57d70-0b88-4afd-bbd1-a456b65b61bf" alt="shot-1" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/5b4e8f6c-f67b-40ab-aee5-b5603bdd8b38" alt="shot-2" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/fb209b74-932b-481e-986a-4274a409bac7" alt="shot-3" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/1feb6c8b-628a-414d-865c-f19ed0f61b18" alt="shot-3" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/6c0356fa-e30f-47f8-a680-b155540f1140" alt="shot-3" width="200"></td>
  </tr>
</table>

<table width="100%">
  <tr>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/1d700dc2-4a1f-4a3f-94c2-f7adb1815285" alt="shot-1" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/446885c0-6723-499c-9f6d-65511b67f524" alt="shot-2" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/0286a267-0d20-435e-b574-ebfef9e2f951" alt="shot-3" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/204a5a42-1c40-4973-b0be-dce2c5c25006" alt="shot-3" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/fb877cb5-4ffa-42e3-82c4-79d421d8d1d3" alt="shot-3" width="200"></td>
  </tr>
</table>

<table width="100%">
  <tr>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/8e16251a-7850-44aa-bc44-f97380994067" alt="shot-1" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/158a7bd0-5a44-4913-aec6-559f729eb59a" alt="shot-2" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/d8508830-d344-46b1-82e2-19893ce87c64" alt="shot-3" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/066a7063-c13b-45c9-8221-e5855308e081" alt="shot-3" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/e50a2e13-aa2a-4645-88df-8ba56bda1163" alt="shot-3" width="200"></td>
  </tr>
</table>


<table width="100%">
  <tr>
    <td align="center" width="25%"><img src="https://github.com/user-attachments/assets/cce7a9b5-a968-4a9f-83c2-c920b302a362" alt="shot-1" width="250"></td>
    <td align="center" width="25%"><img src="https://github.com/user-attachments/assets/8c43e04f-34d6-46ff-80f0-efc3f2417d7e" alt="shot-2" width="250"></td>
    <td align="center" width="25%"><img src="https://github.com/user-attachments/assets/6a1c2b5d-7bfb-441d-984d-fb8e057435d4" alt="shot-3" width="250"></td>
    <td align="center" width="25%"><img src="https://github.com/user-attachments/assets/a3c27df8-5bad-4419-8185-3edf491befc6"" alt="shot-3" width="250"></td>
  </tr>
</table>

---

## 🌦️ Экран Погоды

<table width="100%">
  <tr>
    <td align="center" width="25%"><img src="https://github.com/user-attachments/assets/9041bdd1-de80-436b-b910-af89f4a42926" alt="shot-1" width="250"></td>
    <td align="center" width="25%"><img src="https://github.com/user-attachments/assets/49d73fe3-1f94-4f9d-ad46-923d3311a1d0" alt="shot-2" width="250"></td>
    <td align="center" width="25%"><img src="https://github.com/user-attachments/assets/099831ed-85ea-4089-a614-d1f7140d71a3" alt="shot-3" width="250"></td>
    <td align="center" width="25%"><img src="https://github.com/user-attachments/assets/125ec96c-692e-410e-a6fb-84f62aa5318e" alt="shot-3" width="250"></td>
  </tr>
</table>

- Получение данных с Foreca API.
- В связи с ограничением бесплатного пользования в 30 дней дополнено в случае отсуствия информации поиcк через бесплатные сервисы аэропортов мира
- После ввода названия доп запрашивается код страны для нахождения определением через долготу и ширину
- Верхний тулбар и нижний навигатор доступны.
- Переключение темы и поддержка всех стандартных функций тулбара.

  <tr>
    <td align="center" width="25%"><img src="https://github.com/user-attachments/assets/1dafcbc0-05bf-496f-b5bc-b1b949040859" alt="shot-1" width="230"></td>
    <td align="center" width="25%"><img src="https://github.com/user-attachments/assets/fee677a5-8507-41ad-95fe-6dbb1b57a018" alt="shot-2" width="230"></td>
    <td align="center" width="25%"><img src="https://github.com/user-attachments/assets/dbf3a057-8438-4367-bfb1-ddef6c929c33" alt="shot-3" width="230"></td>
    <td align="center" width="25%"><img src="https://github.com/user-attachments/assets/cc130189-0e20-436c-92b1-62df13fa6e51" alt="shot-3" width="230"></td>
  </tr>
</table>

---

<h2 align="right">💠 Экран Чата</h2>

<table width="100%">
  <tr>
    <td align="center" width="33%">
       <img src="https://github.com/user-attachments/assets/f4776aae-7c2c-4033-aed1-4a8ae9d33b1e" alt="shot-3" width="310">
    </td>
    <td align="center" width="33%">
    <img src="https://github.com/user-attachments/assets/6c7283d0-656b-4d1d-9b8b-8f65547e0c2b" alt="Chat(unreadCount)" width="310">
    </td>
    <td align="center" width="33%">
      <img src="https://github.com/user-attachments/assets/d2e95765-4722-4757-a902-d9328af08dc5" alt="chat(horizontal)" width="310">
    </td>
  </tr>
</table>

- Используется:
    - Реализован на базе FireBase (личка, позже + групповой чат)
- Отображает тулбар и доступ к теме, делиться и другим действиям.
- экран регистрация пользователей (профиль)
- экран списка контактов
- экран обмена текстовыми сообщениями и картинами  

<table width="100%">
  <tr>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/5d8395f5-6b45-43ac-ac09-bef867a47b59" alt="shot-1" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/55a424b8-14e3-4659-9ec1-9f8dd3d267eb" alt="shot-2" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/871db67a-d821-42c1-97a6-5d08bfcb38ce" alt="shot-3" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/d45faae2-1a87-49ac-b991-e49875315764" alt="shot-3" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/682e670c-b37e-4872-add9-795ab7c1a334" alt="shot-3" width="200"></td>
  </tr>
</table>

<table width="100%">
  <tr>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/55bdcaa2-ae2a-4532-bcbc-4a3b43e0ea45" alt="shot-1" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/4f5885db-2f12-4779-97e9-dade5acbc80a" alt="shot-2" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/2584b021-7f40-49c6-a165-58ae633d597b" alt="shot-3" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/c156fdf7-67a3-45c5-9d8b-64e14dbc4b80" alt="shot-3" width="220"></td>
    <td align="center" width="20%"><img src="https://github.com/user-attachments/assets/75a514a9-6474-4934-9d82-7f0ad21a6a73" alt="shot-3" width="200"></td>
  </tr>
</table>

---

## 🛠️ Технические особенности

- Кастомные анимации переходов между экранами (6 видов).
- Реализация `Dropdown` через вложенные вращающие круги.
- Переключение языка и видимости UI в зависимости от локали.
- Прямые переходы (без стека), максимально быстрый отклик и удаление блуждания при длинных переходах.
- Удобный тулбар через `BaseActivity`.
- В каждом экране реализован сообщение на отсутсвии интернета экран очищается для показа ошибки на 3 секунды и затем все обратно 


<h2 align="right">✌️ Стек технологий</h2>
 * Kotlin
 * Android SDK
 * ViewModel + LiveData
 * Coroutines
 * Retrofit
 * Koin (DI)
 * ViewPager2
 * RecyclerView
 * SharedPreferences

---

## 🚀 Будущие доработки

- Расширение возможностей экрана ExtraOption (сделано)
- Оптимизация истории поиска треков и фильмов. (сделано)
- Поддержка более длительного токена погоды.  (сделано)
- Возможность синхронизации истории с облаком.  (сделано)

---

<h2 align="right">⚙️ Установка  и запуск</h2>

1. Клонировать репозиторий:
   git clone https://github.com/amanzhola/playList_Maker
2. Открыть проект в Android Studio
3. Собрать и запустить на эмуляторе или устройстве

---

## 🧑‍💻 Автор / Команда

> **Имя**: Аманжол Аимов
> **Почта**: amanzholaimov@gmail.com  
> **GitHub**: [github.com/amanzhola](https://github.com/amanzhola)

---



