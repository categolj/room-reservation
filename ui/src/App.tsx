import {RoomList} from './components/RoomList'
import {RoomSchedule} from './components/RoomSchedule'
import {BrowserRouter, Link, Route, Routes} from 'react-router-dom'
import {SWRConfig} from 'swr'
import "primereact/resources/themes/lara-light-blue/theme.css"
import "primereact/resources/primereact.min.css"
import "primeicons/primeicons.css"
import "primeflex/primeflex.css"
import './App.css'

function App() {
    // Configure SWR globally
    const swrConfig = {
        revalidateOnFocus: true,
        revalidateIfStale: true,
        revalidateOnReconnect: true,
        refreshInterval: 0, // No automatic polling
        errorRetryCount: 3
    };

    return (
        <SWRConfig value={swrConfig}>
            <BrowserRouter>
                <div className="min-h-screen">
                    <div className="p-4">
                        <div className="text-4xl font-bold text-center mb-4">
                            <Link to="/"
                                  className="text-900 no-underline hover:text-primary cursor-pointer">
                                Room Reservation
                            </Link>
                        </div>
                        <div className="card">
                            <Routes>
                                <Route path="/" element={<RoomList/>}/>
                                <Route path="/:date/rooms" element={<RoomList/>}/>
                                <Route path="/:date/rooms/:roomId" element={<RoomSchedule/>}/>
                            </Routes>
                        </div>
                    </div>
                </div>
            </BrowserRouter>
        </SWRConfig>
    )
}

export default App
