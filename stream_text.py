from fastapi import FastAPI, Request
from fastapi.responses import StreamingResponse, JSONResponse
import asyncio
import json
from threading import Thread, Event, Lock
import time

app = FastAPI()

ACTIVE_STREAMS = {}
MODEL_LOCK = Lock()
print("--- Server sẵn sàng. MODEL_LOCK đã được tạo. ---")


def stream_simulator(job_id: str, stop_event: Event):
    """
    Hàm MÔ PHỎNG AI stream.
    Nó sẽ yield 1 từ mỗi 0.5 giây.
    """
    words = [
        "Đây", "là", "một", "bài", "demo", "streaming", "từ", "FastAPI", 
        "tới", "Spring", "Boot.", "Nếu", "bạn", "thấy", "dòng", "này,", 
        "nó", "đã", "thành", "công!"
    ]

    try:
        for word in words:
            # Kiểm tra xem có tín hiệu STOP không
            if stop_event.is_set():
                print(f"[Streamer {job_id}]: Bị dừng (stopped)!")
                yield "data: [STREAM BỊ DỪNG]\n\n"
                return

            # Stream từng từ
            print(f"[Streamer {job_id}]: Yielding '{word}'")
            yield f"data: {word} \n\n"

            # Giả lập thời gian xử lý (0.5s)
            time.sleep(0.5)

        yield "data: [DONE]\n\n"
        print(f"[Streamer {job_id}]: Hoàn thành bình thường.")

    except Exception as e:
        print(f"[Streamer {job_id}]: Lỗi (có thể do client ngắt kết nối): {e}")

    finally:
        # Luôn dọn dẹp
        ACTIVE_STREAMS.pop(job_id, None)
        print(f"[Streamer {job_id}]: Đã dọn dẹp state.")


def stream_with_lock(job_id: str, stop_event: Event):
    """Quản lý lock (chỉ cho phép 1 model chạy cùng lúc)"""
    try:
        print(f"[Job {job_id}]: Đang chờ 'khóa' MODEL_LOCK...")
        MODEL_LOCK.acquire()
        print(f"[Job {job_id}]: Đã chiếm 'khóa', bắt đầu stream.")
        yield from stream_simulator(job_id, stop_event)
    finally:
        MODEL_LOCK.release()
        print(f"[Job {job_id}]: Đã nhả 'khóa'.")


@app.post("/api/v1/generate/stream")
async def generate_stream(request: Request):
    """Endpoint chính để stream text"""
    data = await request.json()
    job_id = data.get("jobId")

    if not job_id:
        return JSONResponse({"error": "jobId is required"}, status_code=400)

    if job_id in ACTIVE_STREAMS:
        return JSONResponse({"error": "jobId already exists"}, status_code=400)

    print(f"[Server]: Nhận request /stream cho jobId: {job_id}")

    stop_event = Event()
    ACTIVE_STREAMS[job_id] = stop_event

    # StreamingResponse trong FastAPI
    return StreamingResponse(
        stream_with_lock(job_id, stop_event),
        media_type="text/event-stream"
    )


@app.post("/api/v1/generate/stop")
async def generate_stop(request: Request):
    """Endpoint để dừng stream"""
    data = await request.json()
    job_id = data.get("jobId")

    stop_event = ACTIVE_STREAMS.get(job_id)
    if stop_event:
        print(f"[Server]: Nhận tín hiệu /stop cho jobId: {job_id}")
        stop_event.set()
        return JSONResponse({"ok": True, "message": "Stop signal sent"})
    else:
        print(f"[Server]: Nhận tín hiệu /stop cho jobId {job_id} (ĐÃ KẾT THÚC)")
        return JSONResponse(
            {"ok": False, "message": "JobId not found or already completed"},
            status_code=404
        )


if __name__ == "__main__":
    import uvicorn
    print("--- Khởi động FastAPI Server (đa luồng) trên cổng 8001 ---")
    uvicorn.run(app, host="0.0.0.0", port=8001)
